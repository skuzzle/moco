@echo off

for %%f in (target\dot\type-inf\*.dot) do (
    call "C:\Program Files (x86)\Graphviz 2.28\bin\dot.exe" -Tpdf -o %%~dpnf.pdf %%f
)