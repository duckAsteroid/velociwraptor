[Setup]
AppId=Velocipwraptor
AppName=velociwraptor
AppVersion=0.0.1
DefaultDirName={pf}\velociwraptor
DefaultGroupName=Velociwraptor
SourceDir=..\..\..\
SetupIconFile=icons\icon.ico
WizardImageStretch=yes
WizardImageFile=icons\wizard.bmp
Compression=lzma2
SolidCompression=yes
OutputDir=build\distributions
OutputBaseFilename=setup
ChangesEnvironment=yes

[Files]
Source: "build\distributions\velociwraptor-0.0.1\bin\*.*"; DestDir: "{app}\bin"
Source: "build\distributions\velociwraptor-0.0.1\lib\*.*"; DestDir: "{app}\lib"

[Registry]
Root: HKLM; Subkey: "SYSTEM\CurrentControlSet\Control\Session Manager\Environment"; ValueType: expandsz; ValueName: "Path"; ValueData: "{olddata};{app}\bin"

[Icons]
Name: "{group}\Velociwraptor"; Filename: "{app}\bin\velociwraptor.bat"; IconFilename: "icons\icon.ico"
