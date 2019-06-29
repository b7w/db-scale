# -*- coding: utf-8 -*-
from invoke import task


@task()
def dc(c, srv=''):
    with c.cd('etc'):
        c.run('dc down')
        c.run(f'dc up -d {srv};')
        c.run('dc logs -f')


@task()
def wrk_fast(c, srv=''):
    c.run(f'wrk -t 1 -c 4 -d 20s --latency http://127.0.0.1:8080/{srv}/find-one')


@task()
def wrk(c, srv=''):
    c.run(f'wrk -t 1 -c 4 -d 2m --latency http://127.0.0.1:8080/{srv}/find-one')
