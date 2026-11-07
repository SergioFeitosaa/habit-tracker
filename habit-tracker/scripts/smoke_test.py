"""Teste funcional por HTTP; use somente em banco de desenvolvimento (cria registros)."""
import json
import sys
import urllib.request
import urllib.error
import uuid
from datetime import date, timedelta

BASE = (sys.argv[1] if len(sys.argv) > 1 else 'http://localhost:8080') + '/api'
def req(path, method='GET', body=None, token=None, expected=200):
    headers = {'Content-Type': 'application/json'}
    if token: headers['Authorization'] = 'Bearer ' + token
    request = urllib.request.Request(BASE + path, data=json.dumps(body).encode() if body is not None else None, headers=headers, method=method)
    try:
        response = urllib.request.urlopen(request, timeout=10)
        status, raw = response.status, response.read()
    except urllib.error.HTTPError as e:
        status, raw = e.code, e.read()
    assert status == expected, f'{method} {path}: esperado {expected}, recebido {status}: {raw.decode()}'
    return json.loads(raw) if raw else None
suffix = uuid.uuid4().hex
password = 'Teste-123456789!'
def new_user(n):
    email = f'teste-{n}-{suffix}@example.com'
    req('/auth/register','POST',{'name':'Pessoa teste','email':email,'password':password},expected=201)
    return req('/auth/login','POST',{'email':email,'password':password})['token']
a,b = new_user('a'),new_user('b')
req('/habits',expected=401)
h = req('/habits','POST',{'name':'Leitura','description':'Teste'},a,201); id = h['id']
assert any(x['id']==id for x in req('/habits',token=a))
req(f'/habits/{id}',token=b,expected=404)
req(f'/habits/{id}','PUT',{'name':'Leitura diária','description':None},a)
d = '2020-01-01'
req(f'/habits/{id}/completions/{d}','PUT',token=a,expected=204)
req(f'/habits/{id}/completions/{d}','PUT',token=a,expected=204)
req(f'/habits/{id}/completions/2099-01-01','PUT',token=a,expected=400)
history = req(f'/habits/{id}/completions?from=2020-01-01&to=2020-12-31',token=a)
assert history['total'] == 1 and history['dates'] == [d]
req(f'/habits/{id}/completions/{d}','DELETE',token=a,expected=204)
assert req(f'/habits/{id}/completions?from=2020-01-01&to=2020-12-31',token=a)['total'] == 0
req(f'/habits/{id}/completions/{d}','PUT',token=a,expected=204)
req(f'/habits/{id}','DELETE',token=a,expected=204)
assert not any(x['id']==id for x in req('/habits',token=a))
assert any(x['id']==id for x in req('/habits?archived=true',token=a))
assert req(f'/habits/{id}/completions?from=2020-01-01&to=2020-12-31',token=a)['total'] == 1
print('OK: cadastro, login, proteção, isolamento, hábitos, realização, histórico e arquivamento.')
