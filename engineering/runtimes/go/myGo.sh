#

https://go.dev/
# To enable dependency tracking for your code, run the
 go mod init
 go mod init example.com/hello
#   command, giving it the name of the module your code will be in.

# Run the go mod tidy command to synchronize the example.com/hello module's dependencies,
#  adding those required by the code, but not yet tracked in the module.
 go mod tidy

go mod edit -replace example.com/greetings=../greetings
# The command specifies that example.com/greetings should be replaced with ../greetings for the
#  purpose of locating the dependency. After you run the command, the go.mod file in the
#  hello directory should include a replace directive:

# To reference a published module, a go.mod file would typically omit the replace directive and
#  use a require directive with a tagged version number at the end.
require example.com/greetings v1.1.0

go run .

# Ending a file's name with 
_test.go
#  tells the go test command that this file contains test functions.
go test
go test -v

go build

# You can discover the install path by running the go list command, as in the following example:

go list -f '{{.Target}}'

# As an alternative, if you already have a directory like $HOME/bin in your shell path and you'd like to install your Go programs there, you can change the install target by setting the GOBIN variable using the go env command:

go env -w GOBIN=/path/to/your/bin

go install

https://go.dev/doc/tutorial/workspaces
# Add a dependency on the golang.org/x/example module by using go get.
go get golang.org/x/example

# Initialize the workspace
# In the workspace directory, run:
go work init ./hello

# The go command produces a go.work file that looks like this:

go 1.20

use ./hello
# The go.work file has similar syntax to go.mod.
go.work 
# can be used instead of adding replace directives to work across multiple modules.

git clone https://go.googlesource.com/example
go work use ./example

# Learn more about workspaces
# The go command has a couple of subcommands for working with workspaces in addition to go work init which we saw earlier in the tutorial:

# adds a use directive to the go.work file for dir, if it exists, and removes the use directory if the argument directory doesn’t exist. The -r flag examines subdirectories of dir recursively.
go work use [-r] [dir] 
# edits the go.work file similarly to go mod edit
go work edit 
# syncs dependencies from the workspace’s build list into each of the workspace modules.
go work sync 


#