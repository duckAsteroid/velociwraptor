[Setup]
AppId=Velocipwraptor
AppName=velociwraptor
AppVersion=0.0.3
DefaultDirName={commonpf}\velociwraptor
DefaultGroupName=Velociwraptor
SourceDir=..\..\..\
SetupIconFile=icons\icon.ico
WizardSmallImageFile=icons\icon55.bmp
WizardImageStretch=yes
WizardImageFile=icons\wizard.bmp
Compression=lzma2
SolidCompression=yes
OutputDir=build\distributions
OutputBaseFilename=velociwraptor-0.0.3-setup
ChangesEnvironment=yes

[Files]
Source: "build\distributions\velociwraptor-0.0.3\bin\*.*"; DestDir: "{app}\bin"
Source: "build\distributions\velociwraptor-0.0.3\lib\*.*"; DestDir: "{app}\lib"

[Registry]
Root: HKLM; Subkey: "SYSTEM\CurrentControlSet\Control\Session Manager\Environment"; ValueType: expandsz; ValueName: "Path"; ValueData: "{olddata};{app}\bin"

[Icons]
Name: "{group}\Velociwraptor"; Filename: "{app}\bin\velociwraptor.bat"; IconFilename: "icons\icon.ico"
