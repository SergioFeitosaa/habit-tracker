# Executar na raiz do repositório, depois de criar o remoto e autenticar: gh auth login
# Seguro para retomada: ignora títulos já existentes (abertos ou fechados).
$ErrorActionPreference = 'Stop'
if (-not (Get-Command gh -ErrorAction SilentlyContinue)) { throw 'Instale GitHub CLI: https://cli.github.com/' }
gh auth status
if ($LASTEXITCODE -ne 0) { throw 'Autentique-se com gh auth login antes de continuar.' }
$repo = gh repo view --json nameWithOwner --jq '.nameWithOwner'
if ($LASTEXITCODE -ne 0) { throw 'Não foi possível identificar o repositório remoto. Publique-o primeiro.' }
Write-Host "Repositório selecionado: $repo"
$confirm = Read-Host 'Confirme o nome exato acima para cadastrar Issues neste repositório'
if ($confirm -ne $repo) { throw 'Nome não confirmado; nenhuma Issue foi criada.' }
$path = Join-Path $PSScriptRoot '../docs/issues.md'
$text = Get-Content -LiteralPath $path -Raw -Encoding UTF8
$blocks = [regex]::Split($text, '(?m)^## (I\d{2}) — ')
if ($blocks.Count -lt 4) { throw 'Não foram encontradas Issues no arquivo de backlog.' }
$existingJson = gh issue list --repo $repo --state all --limit 1000 --json title
if ($LASTEXITCODE -ne 0) { throw 'Não foi possível listar as Issues.' }
$existing = @($existingJson | ConvertFrom-Json | ForEach-Object { $_.title })
$labelJson = gh label list --repo $repo --limit 1000 --json name
if ($LASTEXITCODE -ne 0) { throw 'Não foi possível listar labels.' }
$labelsExisting = @($labelJson | ConvertFrom-Json | ForEach-Object { $_.name })
for ($i = 1; $i -lt $blocks.Count; $i += 2) {
  $id = $blocks[$i]
  $lines = $blocks[$i+1] -split "`r?`n", 2
  $title = "$id — $($lines[0].Trim())"
  if ($existing -contains $title) { Write-Host "Já existe: $title"; continue }
  $body = $lines[1].Trim()
  $match = [regex]::Match($body, '(?m)^\*\*Labels:\*\* (.+?)\.$')
  if (-not $match.Success) { throw "Labels não encontradas em $title" }
  $labels = @($match.Groups[1].Value -split ',\s*')
  foreach ($label in $labels) {
    if ($labelsExisting -notcontains $label) {
      gh label create $label --repo $repo --color '6b8e75'
      if ($LASTEXITCODE -ne 0) { throw "Falha ao criar label $label." }
      $labelsExisting += $label
    }
  }
  $tmp = [System.IO.Path]::GetTempFileName()
  try {
    [System.IO.File]::WriteAllText($tmp, $body, [System.Text.UTF8Encoding]::new($false))
    $argsList = @('issue','create','--repo',$repo,'--title',$title,'--body-file',$tmp)
    foreach ($label in $labels) { $argsList += @('--label',$label) }
    & gh @argsList
    if ($LASTEXITCODE -ne 0) { throw "Falha ao criar $title. Pode executar novamente após corrigir." }
  } finally { Remove-Item -LiteralPath $tmp -ErrorAction SilentlyContinue }
}
Write-Host 'Concluído. Confira Issues, números e rastreabilidade no GitHub.'
