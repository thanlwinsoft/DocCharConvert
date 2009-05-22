;NSIS Modern User Interface
;DocCharConvert NSIS Installer script
;Written by Keith Stribley
;License: LGPL 2.1 or later

; Some useful definitions that may need changing for different font versions
!ifndef VERSION
  !define VERSION '1.2.2'
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
  !insertmacro MUI_PAGE_LICENSE "lgpl-2.1.txt"
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
  Icon "InstallDCC32.ico"
  UninstallIcon "UninstallDCC32.ico"
  
!ifdef INNER
  !echo "Inner invocation"                  ; just to see what's going on
  OutFile "$%TEMP%\tempinstaller.exe"       ; not really important where this is
  SetCompress off                           ; for speed
  Section "-!${APP_NAME}" SecApp
  WriteUninstaller "$%TEMP%\${APP_NAME}Uninstall.exe"
  SectionEnd
!else
  !echo "Outer invocation"
  RequestExecutionLevel admin
 
!ifdef SIGN
  ; Call makensis again, defining INNER.  This writes an installer for us which, when
  ; it is invoked, will just write the uninstaller to some location, and then exit.
  ; Be sure to substitute the name of this script here.
 
  !system "$\"${NSISDIR}\makensis$\" /DINNER DocCharConvert.nsi" = 0
 
  ; So now run that installer we just created as %TEMP%\tempinstaller.exe.  Since it
  ; calls quit the return value isn't zero.
 
  !system "$%TEMP%\tempinstaller.exe /S /D=$%TEMP%" = 0
 
  ; That will have written an uninstaller binary for us.  Now we sign it with your
  ; favourite code signing tool.
 
 ; /t http://timestamp.verisign.com/scripts/timestamp.dll
  !system "signtool sign /f ..\..\certs\code.thanlwinsoft.pfx /v /d 'DocCharConvert Uninstaller' $%TEMP%\${APP_NAME}Uninstall.exe" = 0

!ENDIF 
  ; Good.  Now we can carry on writing the real installer.
  OutFile "${APP_NAME}-${VERSION}.exe"

;Installer Sections

Function findJavaHome
	StrCpy $0 0
	ReadRegStr $1 HKLM "Software\JavaSoft\Java Runtime Environment\1.7" "JavaHome"
	  StrCmp $1 "" 0 done
	ReadRegStr $1 HKLM "Software\JavaSoft\Java Runtime Environment\1.6" "JavaHome"
	  StrCmp $1 "" 0 done
;	ReadRegStr $1 HKLM "Software\JavaSoft\Java Runtime Environment\1.5" "JavaHome"
;	  StrCmp $1 "" 0 done
	ReadRegStr $1 HKLM "Software\JavaSoft\Java Development Kit\1.7" "JavaHome"
	  StrCmp $1 "" 0 done
	ReadRegStr $1 HKLM "Software\JavaSoft\Java Development Kit\1.6" "JavaHome"
	  StrCmp $1 "" noJava done
;	ReadRegStr $1 HKLM "Software\JavaSoft\Java Development Kit\1.5" "JavaHome"
;	  StrCmp $1 "" noJava done
	
	noJava:
	  StrCpy $1 ""
	  MessageBox MB_YESNO|MB_ICONSTOP "No Java 1.6 Runtime was found. ${APP_NAME} will not work unless Java 1.6 or later is installed. Please download a Java Runtime Environment from http://java.com/en/download/ and rerun this installer. Do you want to proceed anyway?" IDYES done IDNO quit
	quit: 
	Quit
	done:
	Push $1
FunctionEnd

Section "-!${APP_NAME}" SecApp

  Call findJavaHome
  Var /GLOBAL JAVA_HOME
  Pop $JAVA_HOME
  
  
  
  IfFileExists "$INSTDIR\${APP_NAME}\${APP_NAME}Uninstall.exe" 0 BranchNoExist
    
	# uninstall old version
	ExecWait '"$INSTDIR\${APP_NAME}\${APP_NAME}Uninstall.exe" /S _?=$INSTDIR\${APP_NAME}'

    ;MessageBox MB_YESNO|MB_ICONQUESTION "Would you like to overwrite existing ${APP_NAME} directory?" IDNO  NoOverwrite ; skipped if file doesn't exist

    
    BranchNoExist:
  
    SetOverwrite on ; NOT AN INSTRUCTION, NOT COUNTED IN SKIPPINGS

; NoOverwrite:

  SetOutPath "$INSTDIR\${APP_NAME}"
  File /oname=license.txt "lgpl-2.1.txt"
  File DocCharConvert.bat
  File "DocCharConvert32.ico"
  File "UninstallDCC32.ico"

!ifdef SIGN
  File "$%TEMP%\${APP_NAME}Uninstall.exe"
!ELSE
  ;Create uninstaller
  ;WriteUninstaller "$INSTDIR\${APP_NAME}\${APP_NAME}Uninstall.exe"
!ENDIF
  
  !cd "../../doccharconvert"
  File ..\org.thanlwinsoft.doccharconvert.teckit.win32.x86\TecKitJni.dll

  File /r "DocCharConvert\eclipse"
  
  ;File /r "..\release\${APP_NAME}"
    
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
	"$INSTDIR\${APP_NAME}\eclipse\launcher.exe" '-vm "$JAVA_HOME\bin\javaw.exe" -product org.thanlwinsoft.doccharconvert.DocCharConvert' \
	"$INSTDIR\${APP_NAME}\DocCharConvert32.ico" 0 SW_SHOWNORMAL \
	"" "${APP_NAME}"
;  CreateShortCut "$SMPROGRAMS\${APP_NAME}\${APP_NAME}IDE.lnk" \
;	"$INSTDIR\${APP_NAME}\launcher.exe" '-vm "$JAVA_HOME\bin\javaw.exe" -product org.thanlwinsoft.doccharconvert.DocCharConvertIDEProduct' \
;	"$INSTDIR\${APP_NAME}\DocCharConvert32.ico" 0 SW_SHOWNORMAL \
;	"" "${APP_NAME} - developer IDE"
  CreateShortCut "$SMPROGRAMS\${APP_NAME}\${APP_NAME}Uninstall.lnk" \
	"$INSTDIR\${APP_NAME}\${APP_NAME}Uninstall.exe" "" \
	"$INSTDIR\${APP_NAME}\UninstallDCC32.ico" 0 SW_SHOWNORMAL \
	"" "Uninstall ${APP_NAME}"

  CreateShortCut "$DESKTOP\${APP_NAME}.lnk" \
	"$INSTDIR\${APP_NAME}\eclipse\launcher.exe" '-vm "$JAVA_HOME\bin\javaw.exe" -product org.thanlwinsoft.doccharconvert.DocCharConvert' \
	"$INSTDIR\${APP_NAME}\DocCharConvert32.ico" 0 SW_SHOWNORMAL \
	"" "${APP_NAME}"
	
  ;Store installation folder
  WriteRegStr HKLM "Software\${INSTALL_SUFFIX}\${APP_NAME}" "" $INSTDIR


  ; add keys for Add/Remove Programs entry
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}" \
                 "DisplayName" "${APP_NAME} ${VERSION}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}" \
                 "UninstallString" "$INSTDIR\${APP_NAME}\${APP_NAME}Uninstall.exe"
				 
  

SectionEnd

Section "Myanmar" SecMyanmar
	SetOutPath "$INSTDIR\${APP_NAME}\eclipse\plugins"
	File /r repository\plugins\org.thanlwinsoft.doccharconvert.converters.myanmar_*
	SetOutPath "$INSTDIR\${APP_NAME}\eclipse\features"
	File /r repository\features\org.thanlwinsoft.doccharconvert.feature.myanmar_*
    SetOutPath "$INSTDIR\${APP_NAME}"
	File ..\org.thanlwinsoft.doccharconvert.update\nsis\DocCharConvertMyanmar.bat
SectionEnd

Section /o "Mien" SecMien
    SetOutPath "$INSTDIR\${APP_NAME}\eclipse\plugins"
    File /r repository\plugins\org.thanlwinsoft.doccharconvert.converters.mien_*
    SetOutPath "$INSTDIR\${APP_NAME}\eclipse\features"
    File /r repository\features\org.thanlwinsoft.doccharconvert.feature.mien_*
    SetOutPath "$INSTDIR\${APP_NAME}"
    File ..\org.thanlwinsoft.doccharconvert.update\nsis\DocCharConvertMien.bat
SectionEnd

;Optional source - as a compressed archive
Section /o "Source" SecSource
	SetOutPath "$INSTDIR\${APP_NAME}"
	File doccharconvert-*.tar.bz2
SectionEnd

;--------------------------------
;Descriptions

  ;Language strings
  LangString DESC_SecApp ${LANG_ENGLISH} "Install the ${APP_NAME} (version ${VERSION})."
  LangString DESC_SecMyanmar ${LANG_ENGLISH} "Myanmar (Burmese) Font Encoding Converters"
  LangString DESC_SecMien ${LANG_ENGLISH} "Mien Language Script / Encoding Converters"
  LangString DESC_SecSource ${LANG_ENGLISH} "Source Code - only for developers"


  ;Assign language strings to sections
  !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${SecApp} $(DESC_SecApp)
	!insertmacro MUI_DESCRIPTION_TEXT ${SecMyanmar} $(DESC_SecMyanmar)
    !insertmacro MUI_DESCRIPTION_TEXT ${SecMien} $(DESC_SecMien)
    !insertmacro MUI_DESCRIPTION_TEXT ${SecSource} $(DESC_SecSource)
  !insertmacro MUI_FUNCTION_DESCRIPTION_END

Function .onInstFailed
	MessageBox MB_OK "You may want to rerun the installer as an Administrator or specify a different installation directory."

FunctionEnd

Function .onInstSuccess

	Exec '"$INSTDIR\${APP_NAME}\eclipse\launcher.exe" -vm "$JAVA_HOME\bin\javaw.exe"  -product org.thanlwinsoft.doccharconvert.DocCharConvert'

FunctionEnd

!endif
;--------------------------------
;Uninstaller Section

Section "Uninstall"
  SetShellVarContext all
  IfFileExists "$INSTDIR" AppFound 0
    MessageBox MB_OK|MB_ICONEXCLAMATION "$INSTDIR\${APP_NAME} was not found! You may need to uninstall manually." 
	
AppFound:
  RMDir /REBOOTOK /r "$INSTDIR\eclipse"
  Delete /REBOOTOK "$INSTDIR\TecKitJni.dll"
  Delete /REBOOTOK "$INSTDIR\DocCharConvert*.*"
  Delete /REBOOTOK "$INSTDIR\UninstallDCC32.ico"
  Delete /REBOOTOK "$INSTDIR\setJavaHome.bat"
  Delete /REBOOTOK "$INSTDIR\lgpl-2.1.txt"
  Delete /REBOOTOK "$INSTDIR\license.txt"
  
  ;Delete "$INSTDIR\${SRC_ARCHIVE}"
  Delete /REBOOTOK "$INSTDIR\${APP_NAME}Uninstall.exe"

  RMDir "$INSTDIR"
  
  Delete  "$DESKTOP\${APP_NAME}.lnk"
  RMDir /r "$SMPROGRAMS\${APP_NAME}"

  DeleteRegKey /ifempty HKLM "Software\${INSTALL_SUFFIX}"
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APP_NAME}"
  
  IfFileExists "$INSTDIR\workspace" 0 end
    MessageBox MB_YESNO|MB_ICONEXCLAMATION "$INSTDIR\workspace exists. This may contain some of your own files. Do you want to remove it as well?" IDYES 0 IDNO end

end:

SectionEnd

