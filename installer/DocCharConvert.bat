set OPENOFFICE_BASE="$oo_home"
set CLASSPATH="$INSTALL_PATH\DocCharConvert.jar"
rem for %%i in (%OPENOFFICE_BASE%/program/classes/*.jar) do set CLASSPATH=%CLASSPATH%;%%i
rem "$JAVA_HOME\bin\java" DocCharConvert/MainForm %1 %2 %3 %4 %5 %6 %7 %8
"$JAVA_HOME\bin\javaw" -jar "$INSTALL_PATH\DocCharConvert.jar"
