# cwtools-vic2-config
.cwt config files for Victoria II

To use these copy the contents of this repository and place it in a folder called `.cwtools` in the folder you open in vscode. E.g. if your mod is located at `Victoria 2\mod\my_mod`, you would extract this folder into `Victoria 2\mod\my_mod\.cwtools`.

See https://github.com/simsulla/cwtools/wiki/.cwt-config-file-guidance for guidance on the file format

### Contributing
If you'd like to contribute, press the pen icon on any file, then press "Create a new branch for this commit and start a pull request". You can then make further changes as a "pull request". When done, mention it in the pull request and your changes will be included.

### Build and Run 
For development and running use VS Code. Clone or fork github.com tboby/cwtools into one folder. Clone tboby/cwtools-vscode into another.

1. Open cwtools folder
1. Open a new terminal in VS Code
1. cd ./CWTools
1. dotnet build (for the core library project, that single command should be enough to restore all dependencies and build)
1. correct any compile errors and dotnet build again.
1. cd ../CWToolsTests
1. dotnet run

1. Open the cwtools-vscode folder (usually in another window)
1. Edit packet.json and packet.dependencies setting git url to your fork and git file to your local cwtools folder.
1. Open a new terminal in VS Code
1. ./.paket/paket.exe update -g git (This tells it to pull the latest core library commit from the other repo locally)
1. In the debug panel on the left, run the launch profile "Quick Build and Launch Extension"
1. VS Code will build the language server, build the extension, then launch the extension in a new window (with the title "Extension host" or something)
1. Open the game folder you are testing
