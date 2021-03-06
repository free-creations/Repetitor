# see also http://www.klopfenstein.net/lorenz.aspx/simple-nsis-installer-with-user-execution-level

# Included files
!include Sections.nsh
!include "MUI.nsh"
!include "StringReplace.nsh"

Name "Repetitor"


# General Symbol Definitions
!define REGKEY "SOFTWARE\$(^Name)"
!define VERSION 0.1.1
!define COMPANY "free-creations"
!define URL www.free-creations.de

!define MEDIADIR "$MUSIC\Repetitor"

RequestExecutionLevel user




# Reserved Files
ReserveFile "${NSISDIR}\Plugins\x86-unicode\StartMenu.dll"

# Variables
Var StartMenuGroup

# Installer pages
!insertmacro MUI_PAGE_WELCOME
#!insertmacro MUI_PAGE_LICENSE ..\LICENSE-2.0.txt
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES



!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES


#Page license
#Page directory
#Page custom StartMenuGroupSelect "" ": $(StartMenuPageTitle)"
#Page instfiles

# Installer languages
!insertmacro MUI_LANGUAGE "English"
!insertmacro MUI_LANGUAGE "German"
!insertmacro MUI_LANGUAGE "French"

#LoadLanguageFile "${NSISDIR}\Contrib\Language files\English.nlf"
#LoadLanguageFile "${NSISDIR}\Contrib\Language files\German.nlf"
#LoadLanguageFile "${NSISDIR}\Contrib\Language files\French.nlf"

# Installer attributes
OutFile dist\install-repetitor.exe
InstallDir "$LOCALAPPDATA\Repetitor-${VERSION}"
CRCCheck on
XPStyle on
Icon "${NSISDIR}\Contrib\Graphics\Icons\orange-install-nsis.ico"
ShowInstDetails show
AutoCloseWindow false
LicenseData ..\LICENSE-2.0.txt
VIProductVersion 0.1.1.0
VIAddVersionKey /LANG=${LANG_ENGLISH} ProductName Repetitor
VIAddVersionKey /LANG=${LANG_ENGLISH} ProductVersion "${VERSION}"
VIAddVersionKey /LANG=${LANG_ENGLISH} CompanyName "${COMPANY}"
VIAddVersionKey /LANG=${LANG_ENGLISH} CompanyWebsite "${URL}"
VIAddVersionKey /LANG=${LANG_ENGLISH} FileVersion "${VERSION}"
VIAddVersionKey /LANG=${LANG_ENGLISH} FileDescription ""
VIAddVersionKey /LANG=${LANG_ENGLISH} LegalCopyright ""
InstallDirRegKey HKCU "${REGKEY}" Path
UninstallIcon "${NSISDIR}\Contrib\Graphics\Icons\orange-uninstall-nsis.ico"
ShowUninstDetails show

# Installer sections
Section -Main SEC0000
    SetShellVarContext current
    
    # The example Media
    SetOverwrite off
    SetOutPath ${MEDIADIR} 
    File /r ..\exampleMedia\*
    
    # The Java Runtime
    SetOverwrite on
    SetOutPath $INSTDIR\jre7
    File /r "C:\Program Files (x86)\Java\jre7\*"


    # The Repetitor application (unzip before running this)
    SetOutPath $INSTDIR
    File /r ..\dist\repetitor\repetitor\*
    
    SetOutPath $INSTDIR\bin
    File ..\Install\specialFiles\repetitor.exe
    WriteRegStr HKCU "${REGKEY}\Components" Main 1
    SetOutPath $INSTDIR\etc
    File ..\Install\specialFiles\repetitor.conf
    
    # Write the prefences file for the example Media
    SetOutPath "$APPDATA\.repetitor\dev\config\Preferences\de\free_creations\"     
    FileOpen $0 "mediaContainerExplorer2.properties" w
    # replace single slash by double slash using the strRep function
    ${StrRep} '$1' '${MEDIADIR}' '\' '\\'
    fileWrite $0 "mediafolder=$1"
    # close the file
    fileClose $0
       
SectionEnd

Section -post SEC0001
    SetShellVarContext current
    WriteRegStr HKCU "${REGKEY}" Path $INSTDIR
    WriteRegStr HKCU "${REGKEY}" StartMenuGroup $StartMenuGroup
    SetOutPath $INSTDIR
    WriteUninstaller $INSTDIR\uninstall.exe
    SetOutPath $SMPROGRAMS\$StartMenuGroup
    CreateShortcut "$SMPROGRAMS\$StartMenuGroup\Repetitor.lnk" $INSTDIR\bin\repetitor.exe
    CreateShortcut "$DESKTOP\Repetitor.lnk" $INSTDIR\bin\repetitor.exe
    ##CreateShortcut "$SMPROGRAMS\$StartMenuGroup\$(^UninstallLink).lnk" $INSTDIR\uninstall.exe
    
    WriteRegStr HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayName "$(^Name)"
    WriteRegStr HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayVersion "${VERSION}"
    WriteRegStr HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" Publisher "${COMPANY}"
    WriteRegStr HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" URLInfoAbout "${URL}"
    WriteRegStr HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayIcon $INSTDIR\uninstall.exe
    WriteRegStr HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" UninstallString $INSTDIR\uninstall.exe
    WriteRegDWORD HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" NoModify 1
    WriteRegDWORD HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" NoRepair 1
SectionEnd

# Macro for selecting uninstaller sections
!macro SELECT_UNSECTION SECTION_NAME UNSECTION_ID
    Push $R0
    ReadRegStr $R0 HKCU "${REGKEY}\Components" "${SECTION_NAME}"
    StrCmp $R0 1 0 next${UNSECTION_ID}
    !insertmacro SelectSection "${UNSECTION_ID}"
    GoTo done${UNSECTION_ID}
next${UNSECTION_ID}:
    !insertmacro UnselectSection "${UNSECTION_ID}"
done${UNSECTION_ID}:
    Pop $R0
!macroend

# Uninstaller sections
Section /o -un.Main UNSEC0000
    Delete /REBOOTOK $INSTDIR\bin\repetitor.exe
    ##Delete /REBOOTOK $MUSIC\JazzMass.fmc
    RmDir /r /REBOOTOK $INSTDIR
    DeleteRegValue HKCU "${REGKEY}\Components" Main
SectionEnd

Section -un.post UNSEC0001
    DeleteRegKey HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)"
    Delete /REBOOTOK "$SMPROGRAMS\$StartMenuGroup\$(^UninstallLink).lnk"
    Delete /REBOOTOK "$SMPROGRAMS\$StartMenuGroup\repetitor.lnk"
    Delete /REBOOTOK "$DESKTOP\repetitor.lnk" 
    Delete /REBOOTOK $INSTDIR\uninstall.exe
    DeleteRegValue HKCU "${REGKEY}" StartMenuGroup
    DeleteRegValue HKCU "${REGKEY}" Path
    DeleteRegKey /IfEmpty HKCU "${REGKEY}\Components"
    DeleteRegKey /IfEmpty HKCU "${REGKEY}"
    RmDir /REBOOTOK $SMPROGRAMS\$StartMenuGroup
    RmDir /REBOOTOK $INSTDIR
SectionEnd

# Installer functions
Function StartMenuGroupSelect
    Push $R1
    StartMenu::Select /autoadd /text "$(StartMenuPageText)" /lastused $StartMenuGroup Repetitor
    Pop $R1
    StrCmp $R1 success success
    StrCmp $R1 cancel done
    MessageBox MB_OK $R1
    Goto done
success:
    Pop $StartMenuGroup
done:
    Pop $R1
FunctionEnd

#--- Installer init functions
Function .onInit
    InitPluginsDir
    # Auto- uninstall old before installing new
    # ..search for registry key
    ReadRegStr $R0 HKCU \
                   "Software\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" \
                   "UninstallString"
    StrCmp $R0 "" done
    
    #.. ask user if OK to uninstall
   # MessageBox MB_OKCANCEL|MB_ICONEXCLAMATION \
   #           "$(^Name) is already installed. $\n$\nClick `OK` to remove the \
   #            previous version or `Cancel` to cancel this upgrade." \
   #            IDOK uninst
   #            Abort
   MessageBox MB_OKCANCEL|MB_ICONEXCLAMATION \
              $(allreadyInstalled) \
               IDOK uninst
               Abort

    ;Run the uninstaller
uninst:
    ClearErrors
    ExecWait '$R0 _?=$INSTDIR' ;Do not copy the uninstaller to a temp file
 
    IfErrors no_remove_uninstaller done
      ;You can either use Delete /REBOOTOK in the uninstaller or add some code
      ;here to remove the uninstaller. Use a registry key to check
      ;whether the user has chosen to uninstall. If you are using an uninstaller
      ;components page, make sure all sections are uninstalled.
   no_remove_uninstaller:
 
done:

FunctionEnd

#--- Uninstaller functions
Function un.onInit
    ReadRegStr $INSTDIR HKCU "${REGKEY}" Path
    ReadRegStr $StartMenuGroup HKCU "${REGKEY}" StartMenuGroup
    !insertmacro SELECT_UNSECTION Main ${UNSEC0000}
FunctionEnd

# Installer Language Strings
# TODO Update the Language Strings with the appropriate translations.

LangString allreadyInstalled ${LANG_ENGLISH} "$(^Name) is already installed. $\n$\nClick `OK` to remove the \
                                              previous version or `Cancel` to cancel this upgrade." 
LangString allreadyInstalled ${LANG_GERMAN} "$(^Name) ist schon installiert. $\n$\n `OK` entfernt \
                                              die alte Version und installert die neue Version; `Abbrechen` stoppt die Installation." 
LangString allreadyInstalled ${LANG_FRENCH} "$(^Name) est deja installé. $\n$\n `OK` désinstal \
                                              l'ancien version; `Cancel` arrete l'installation." 

LangString StartMenuPageTitle ${LANG_ENGLISH} "Start Menu Folder"
LangString StartMenuPageTitle ${LANG_GERMAN} "Start Menu Folder"
LangString StartMenuPageTitle ${LANG_FRENCH} "Start Menu Folder"

LangString StartMenuPageText ${LANG_ENGLISH} "Select the Start Menu folder in which to create the program's shortcuts:"
LangString StartMenuPageText ${LANG_GERMAN} "Select the Start Menu folder in which to create the program's shortcuts:"
LangString StartMenuPageText ${LANG_FRENCH} "Select the Start Menu folder in which to create the program's shortcuts:"

LangString ^UninstallLink ${LANG_ENGLISH} "Uninstall $(^Name)"
LangString ^UninstallLink ${LANG_GERMAN} "Uninstall $(^Name)"
LangString ^UninstallLink ${LANG_FRENCH} "Uninstall $(^Name)"
