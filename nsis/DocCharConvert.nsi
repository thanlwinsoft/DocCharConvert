;NSIS Modern User Interface
;DocCharConvert NSIS Installer script
;Written by Keith Stribley

; Some useful definitions that may need changing for different font versions
!ifndef VERSION
  !define VERSION '1.0.2'
!endif

!define APP_NAME 'DocCharConvert'
;!define SRC_ARCHIVE "..\org.thanlwinsoft.doccharconvert-src-${VERSION}.tgz"
!define INSTALL_SUFFIX "ThanLwinSoft.org"

;--------------------------------
;Include Modern UI

  !include "MUI.nsh"

;--------------------------------
;General

  ;Name and file
  Name "${APP_NAME} (${VERSION})"
  Caption "A Program for converting between different Character encodings."

  OutFile "${APP_NAME}-${VERSION}.exe"
  ;OutFile "${FONT_REG_FILE}"
  ;OutFile "${FONT_BOLD_FILE}"
  InstallDir $PROGRAMFILES\${INSTALL_SUFFIX}

  
  ;Get installation folder from registry if available
  InstallDirRegKey HKLM "Software\${INSTALL_SUFFIX}\${APP_NAME}" ""
  
  SetCompressor lzma

;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING

;--------------------------------
;Pages

  !insertmacro MUI_PAGE_WELCOME
  !insertmacro MUI_PAGE_LICENSE "../license.txt"
  !insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro MUI_PAGE_INSTFILES
  !insertmacro MUI_PAGE_FINISH
  
  !insertmacro MUI_UNPAGE_WELCOME
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
  !insertmacro MUI_UNPAGE_FINISH
  
;--------------------------------
;Languages
 
  !insertmacro MUI_LANGUAGE "English"

;--------------------------------
  Icon "DocCharConvert32.ico"
  UninstallIcon "UninstallDCC16.ico"
;Installer Sections

Function findJavaHome
	StrCpy $0 0
	ReadRegStr $1 HKLM "Software\JavaSoft\Java Runtime Environment\1.7" "JavaHome"
	  StrCmp $1 "" 0 done
	ReadRegStr $1 HKLM "Software\JavaSoft\Java Runtime Environment\1.6" "JavaHome"
	  StrCmp $1 "" 0 done
	ReadRegStr $1 HKLM "Software\JavaSoft\Java Runtime Environment\1.5" "JavaHome"
	  StrCmp $1 "" 0 done
	ReadRegStr $1 HKLM "Software\JavaSoft\Java Development Kit\1.7" "JavaHome"
	  StrCmp $1 "" 0 done
	ReadRegStr $1 HKLM "Software\JavaSoft\Java Development Kit\1.6" "JavaHome"
	  StrCmp $1 "" 0 done
	ReadRegStr $1 HKLM "Software\JavaSoft\Java Development Kit\1.5" "JavaHome"
	  StrCmp $1 "" noJava done
	
	noJava:
	  StrCpy $1 ""
	  MessageBox MB_YESNO|MB_ICONSTOP "No Java 1.5 Runtime was found. ${APP_NAME} will not work unless a Java is installed. Please download a Java Runtime Environment from http://java.com/en/download/ and rerun this installer. Do you want to proceed anyway?" IDYES done IDNO quit
	quit: 
	Quit
	done:
	Push $1
FunctionEnd

Section "-!${APP_NAME}" SecApp

  Call findJavaHome
  Var /GLOBAL JAVA_HOME
  Pop $JAVA_HOME
  
  
  
  IfFileExists "$INSTDIR" 0 BranchNoExist
    
    MessageBox MB_YESNO|MB_ICONQUESTION "Would you like to overwrite existing ${APP_NAME} directory?" IDNO  NoOverwrite ; skipped if file doesn't exist

    
    BranchNoExist:
  
    SetOverwrite on ; NOT AN INSTRUCTION, NOT COUNTED IN SKIPPINGS

NoOverwrite:

  
  SetOutPath "$INSTDIR"
  
  File /r "${APP_NAME}"
  
  SetOutPath "$INSTDIR\${APP_NAME}"
  File /oname=license.txt "..\license.txt"
  File DocCharConvert.bat
  File "DocCharConvert32.ico"
  File "UninstallDCC16.ico"
  
  SetOutPath "$INSTDIR\${APP_NAME}\configuration\org.thanlwinsoft.doccharconvert\Converters"
  File "/oname=TecKitJni.dll" "..\Converters\TecKitJni.dll" 
  
  ClearErrors
  ; write a batch file that sets the Java home
  FileOpen $0 "$INSTDIR\${APP_NAME}\setJavaHome.bat" w
  FileWrite $0 "set JAVA_HOME=$JAVA_HOME"
  FileWriteByte $0 "13"
  FileWriteByte $0 "10"
  FileClose $0
  IfErrors doneJavaHomeError doneJavaHomeBat
  doneJavaHomeError:
  	MessageBox MB_OK|MB_ICONEXCLAMATION "Failed to write setJavaHome.bat"
  doneJavaHomeBat:
  
  SetShellVarContext all
  ; set up shortcuts
  CreateDirectory "$SMPROGRAMS\${APP_NAME}"
  CreateShortCut "$SMPROGRAMS\${APP_NAME}\${APP_NAME}.lnk" \
	"$INSTDIR\${APP_NAME}\eclipse.exe" '-vm "$JAVA_HOME\bin\javaw.exe"' \
	"$INSTDIR\${APP_NAME}\DocCharConvert32.ico" 0 SW_SHOWNORMAL \
	"" "${APP_NAME}"
  CreateShortCut "$SMPROGRAMS\${APP_NAME}\${APP_NAME}Uninstall.lnk" \
	"$INSTDIR\${APP_NAME}\Uninstall.exe" "" \
	"$INSTDIR\${APP_NAME}\UninstallDCC16.ico" 0 SW_SHOWNORMAL \
	"" "Uninstall ${APP_NAME}"

  CreateShortCut "$DESKTOP\${APP_NAME}.lnk" \
	"$INSTDIR\${APP_NAME}\eclipse.exe" '-vm "$JAVA_HOME\bin\javaw.exe"' \
	"$INSTDIR\${APP_NAME}\DocCharConvert32.ico" 0 SW_SHOWNORMAL \
	"" "${APP_NAME}"
	
  ;Store installation folder
  WriteRegStr HKLM "Software\${INSTALL_SUFFIX}\${APP_NAME}" "" $INSTDIR

  
  ;Create uninstaller
  WriteUninstaller "$INSTDIR\${APP_NAME}\Uninstall.exe"

  ; add keys for Add/Remove Programs entry
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}" \
                 "DisplayName" "${APP_NAME} ${VERSION}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}" \
                 "UninstallString" "$INSTDIR\${APP_NAME}\Uninstall.exe"
				 
  

SectionEnd

Section "Myanmar" SecMyanmar
	SetOutPath "$INSTDIR\${APP_NAME}\configuration\org.thanlwinsoft.doccharconvert\Converters"
	SetOverwrite on
	
	File "..\Converters\Academy.dccx" 
	File "..\Converters\academy.tec"
	File "..\Converters\academy.xml"
	File "..\Converters\WinMyanmar.tec"
	File "..\Converters\WinMyanmar.xml"
	File "..\Converters\Winnwa.dccx" 
	File "..\Converters\Wwin_burmese.tec" 
	File "..\Converters\Wwin_burmese.xml" 
	File "..\Converters\Wwin_burmese.dccx" 
	File "..\Converters\IwinMedium.dccx" 
	File "..\Converters\IWinMedi.xml" 
	File "..\Converters\IWinMedi.tec" 
	File "..\Converters\MyanmarUni4ToUni5.dccx" 
	File "..\Converters\myUni4ToUni5.xml" 
SectionEnd

;Optional source - as a compressed archive
Section /o "Source code" SecSrc

  SetOutPath "$INSTDIR"
  SetOverwrite on
  ;ADD YOUR OWN FILES HERE...
  ;File "${SRC_ARCHIVE}"
  
SectionEnd


;--------------------------------
;Descriptions

  ;Language strings
  LangString DESC_SecApp ${LANG_ENGLISH} "Install the ${APP_NAME} (version ${VERSION})."
  LangString DESC_SecMyanmar ${LANG_ENGLISH} "Myanmar Font Encoding Converters"
  LangString DESC_SecSrc ${LANG_ENGLISH} "Install the source code for ${APP_NAME} (version ${VERSION}). You only need this if you are a software developer."


  ;Assign language strings to sections
  !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${SecApp} $(DESC_SecApp)
	!insertmacro MUI_DESCRIPTION_TEXT ${SecMyanmar} $(DESC_SecMyanmar)
    !insertmacro MUI_DESCRIPTION_TEXT ${SecSrc} $(DESC_SecSrc)
  !insertmacro MUI_FUNCTION_DESCRIPTION_END

Function .onInstFailed
	MessageBox MB_OK "You may want to rerun the installer as an Administrator or specify a different installation directory."

FunctionEnd

Function .onInstSuccess

	Exec '"$INSTDIR\${APP_NAME}\eclipse.exe" -vm "$JAVA_HOME\bin\javaw.exe"'

FunctionEnd

;--------------------------------
;Uninstaller Section

Section "Uninstall"
  SetShellVarContext all
  
  IfFileExists "$INSTDIR" AppFound 0
    MessageBox MB_OK|MB_ICONEXCLAMATION "$INSTDIR\${APP_NAME} was not found! You may need to uninstall manually." 
	
AppFound:
  RMDir /r "$INSTDIR\configuration"
  RMDir /REBOOTOK /r "$INSTDIR\plugins"
  Delete /REBOOTOK "$INSTDIR\eclipse.exe"
  Delete /REBOOTOK "$INSTDIR\startup.jar"
  Delete /REBOOTOK "$INSTDIR\.eclipseproduct"
  Delete /REBOOTOK "$INSTDIR\DocCharConvert*.*"
  Delete /REBOOTOK "$INSTDIR\UninstallDCC16.ico"
  Delete /REBOOTOK "$INSTDIR\setJavaHome.bat"
  Delete /REBOOTOK "$INSTDIR\license.txt"
  
  ;Delete "$INSTDIR\${SRC_ARCHIVE}"
  Delete /REBOOTOK "$INSTDIR\Uninstall.exe"

  RMDir "$INSTDIR"
  
  Delete  "$DESKTOP\${APP_NAME}.lnk"
  RMDir /r "$SMPROGRAMS\${APP_NAME}"

  DeleteRegKey /ifempty HKLM "Software\${INSTALL_SUFFIX}"
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}"
  
  IfFileExists "$INSTDIR\workspace" 0 end
    MessageBox MB_YESNO|MB_ICONEXCLAMATION "$INSTDIR\workspace exists. This may contain some of your own files. Do you want to remove it as well?" IDYES 0 IDNO end
  
  RMDir /REBOOTOK /r "$INSTDIR"

end:

SectionEnd

