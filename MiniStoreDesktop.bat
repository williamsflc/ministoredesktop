rem @echo off

rem Verificar que Java 21 o superior esta instalado en el equipo
rem Colocar la ruta de java
SET JAVA_EXEC=java


SET MCLASS=com.lasopro.ministore.desktop.MinistoreDesktop
SET CSPATH=./target/lib/*.jar;./target/MinistoreDesktop-1.0-SNAPSHOT.jar
%JAVA_EXEC% -splash:res/splash.gif -cp %CSPATH% %MCLASS%
pause