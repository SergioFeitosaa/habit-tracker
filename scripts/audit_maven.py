#!/usr/bin/env python3
"""Check resolved Maven artifacts against GitHub's public advisory database."""
import json
import os
from pathlib import Path
import sys
import time
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode
from urllib.request import Request, urlopen

API = 'https://api.github.com/advisories'


def components(node):
    """Return unique resolved artifacts, including test and transitive dependencies."""
    found = set()
    stack = list(node.get('children', []))
    while stack:
        current = stack.pop()
        if all(current.get(k) for k in ('groupId', 'artifactId', 'version')):
            found.add((current['groupId'], current['artifactId'], current['version']))
        stack.extend(current.get('children', []))
    return sorted(found)


def advisories(group, artifact, version):
    params = urlencode({'ecosystem': 'maven', 'affects': f'{group}:{artifact}@{version}', 'per_page': 100})
    url = f'{API}?{params}'
    result = []
    while url:
        headers = {'Accept': 'application/vnd.github+json', 'User-Agent': 'habit-tracker-dependency-audit', 'X-GitHub-Api-Version': '2022-11-28'}
        if os.getenv('GITHUB_TOKEN'):
            headers['Authorization'] = 'Bearer ' + os.environ['GITHUB_TOKEN']
        request = Request(url, headers=headers)
        for attempt in range(3):
            try:
                with urlopen(request, timeout=30) as response:
                    result.extend(json.load(response))
                    links = response.headers.get('Link', '')
                break
            except (HTTPError, URLError, TimeoutError) as exc:
                if attempt == 2:
                    raise RuntimeError(f'Falha ao consultar {group}:{artifact}:{version}: {exc}') from exc
                time.sleep(2 ** attempt)
        url = None
        for link in links.split(','):
            if 'rel="next"' in link:
                url = link.split(';', 1)[0].strip().strip('<>')
    return result


def safe(text):
    return str(text).replace('\r', ' ').replace('\n', ' ').replace('|', '\\|')


def main():
    if len(sys.argv) != 3:
        raise SystemExit('Uso: audit_maven.py arvore.json relatorio.md')
    tree = json.loads(Path(sys.argv[1]).read_text(encoding='utf-8'))
    packages = components(tree)
    if not packages:
        raise SystemExit('Nenhuma dependência encontrada; a análise não foi executada.')
    findings = []
    for group, artifact, version in packages:
        for advisory in advisories(group, artifact, version):
            findings.append((f'{group}:{artifact}:{version}', advisory))
    report = ['# Auditoria Maven', '', f'Componentes resolvidos consultados (incluindo teste): {len(packages)}.',
              f'Advisories encontrados: {len(findings)}.', '',
              'A correspondência de versão não prova que o cenário seja explorável. Analise cada advisory e o escopo da dependência.', '']
    if findings:
        report.extend(['| Dependência | Severidade | Advisory | Resumo |', '|---|---|---|---|'])
        for package, advisory in findings:
            ghsa = safe(advisory.get('ghsa_id', 'desconhecido'))
            url = advisory.get('html_url', '')
            report.append(f"| {safe(package)} | {safe(advisory.get('severity', 'desconhecida'))} | [{ghsa}]({url}) | {safe(advisory.get('summary', ''))} |")
            print(f"::warning::{safe(package)}: {ghsa} ({safe(advisory.get('severity', 'desconhecida'))})")
    else:
        report.append('Nenhum advisory retornado para as versões consultadas. Isso não garante ausência de vulnerabilidades.')
    text = '\n'.join(report) + '\n'
    Path(sys.argv[2]).write_text(text, encoding='utf-8')
    if os.getenv('GITHUB_STEP_SUMMARY'):
        with open(os.environ['GITHUB_STEP_SUMMARY'], 'a', encoding='utf-8') as summary:
            summary.write(text)
    print(f'Análise concluída: {len(packages)} componentes, {len(findings)} advisories. Consulte o relatório.')


if __name__ == '__main__':
    main()
