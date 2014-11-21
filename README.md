lock-free_algorithms
====================

Linux
----
http://linux.die.net/man/1/taskset

Para controlar los core con que se ejecuta la tarea

taskset -c 0,1 ant -f build.xml
taskset -c 0,1 ant -f build.xml
taskset -c 0,2 ant -f build.xml

numactl -N0 -m0 ant ...

0 y 2 es el mismo core

Windows
-----
http://ss64.com/nt/start.html

START "title" /AFFINITY "command"