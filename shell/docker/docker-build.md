#

https://docs.docker.com/build/

#

The Docker Engine uses a client-server architecture and is composed of multiple components and tools. The most common method of executing a build is by issuing a docker build command. The CLI sends the request to Docker Engine which, in turn, executes your build.

The new client Docker Buildx, is a CLI plugin that extends the docker command with the full support of the features provided by BuildKit builder toolkit. docker buildx build command provides the same user experience as docker build with many new features like creating scoped builder instances, building against multiple nodes concurrently, outputs configuration, inline build caching, and specifying target platform. In addition, Buildx also supports new features that aren’t yet available for regular docker build like building manifest lists, distributed caching, and exporting build results to OCI image tarballs.

https://docs.docker.com/build/install-buildx/

Set Buildx as the default builder🔗
--

Running the command docker buildx install sets up the docker build command as an alias to docker buildx. This results in the ability to have docker build use the current Buildx builder.

To remove this alias, run docker buildx uninstall.

Dockerfile
--

It all starts with a Dockerfile.
Packaging your software
https://docs.docker.com/build/building/packaging/

Docker builds images by reading the instructions from a Dockerfile. This is a text file containing instructions that adhere to a specific format needed to assemble your application into a container image and for which you can find its specification reference in the Dockerfile reference.
https://docs.docker.com/engine/reference/builder/

Docker can build images automatically by reading the instructions from a Dockerfile. A Dockerfile is a text document that contains all the commands a user could call on the command line to assemble an image.
Format
Here is the format of the Dockerfile:

    # Comment
    INSTRUCTION arguments

Docker runs instructions in a Dockerfile in order. A Dockerfile must begin with a FROM instruction. This may be after parser directives, comments, and globally scoped ARGs.

Docker treats lines that begin with # as a comment, unless the line is a valid parser directive. A # marker anywhere else in a line is treated as an argument.

Comment lines are removed before the Dockerfile instructions are executed,

Note however, that whitespace in instruction arguments, such as the commands following RUN, are preserved,

    RUN echo "\
        hello\
        world"

Parser directives
--

Parser directives are optional, and affect the way in which subsequent lines in a Dockerfile are handled. Parser directives do not add layers to the build, and will not be shown as a build step. Parser directives are written as a special type of comment in the form # directive=value. A single directive may only be used once.

Once a comment, empty line or builder instruction has been processed, Docker no longer looks for parser directives. Instead it treats anything formatted as a parser directive as a comment and does not attempt to validate if it might be a parser directive. Therefore, all parser directives must be at the very top of a Dockerfile.

The following parser directives are supported:

    syntax
    escape

Environment replacement
--

Environment variables (declared with the ENV statement) can also be used in certain instructions as variables to be interpreted by the Dockerfile. Escapes are also handled for including variable-like syntax into a statement literally.

Environment variables are notated in the Dockerfile either with $variable_name or ${variable_name}. They are treated equivalently and the brace syntax is typically used to address issues with variable names with no whitespace, like ${foo}_bar.

The ${variable_name} syntax also supports a few of the standard bash modifiers as specified below:

${variable:-word} indicates that if variable is set then the result will be that value. If variable is not set then word will be the result.
${variable:+word} indicates that if variable is set then word will be the result, otherwise the result is the empty string.
In all cases, word can be any string, including additional environment variables.

Escaping is possible by adding a \ before the variable: \$foo or \${foo}, for example, will translate to $foo and ${foo} literals respectively.

    FROM busybox
    ENV FOO=/bar
    WORKDIR ${FOO}   # WORKDIR /bar
    ADD . $FOO       # ADD . /bar
    COPY \$FOO /quux # COPY $FOO /quux

.dockerignore file
--

Before the docker CLI sends the context to the docker daemon, it looks for a file named .dockerignore in the root directory of the context. If this file exists, the CLI modifies the context to exclude files and directories that match patterns in it. This helps to avoid unnecessarily sending large or sensitive files and directories to the daemon and potentially adding them to images using ADD or COPY.

Here is an example .dockerignore file:

    # comment
    */temp*
    */*/temp*
    temp?

    Rule        Behavior

    # comment   Ignored.
    */temp*	    Exclude files and directories whose names start with temp in any immediate subdirectory of the root. For example, the plain file /somedir/temporary.txt is excluded, as is the directory /somedir/temp.
    */*/temp*   Exclude files and directories starting with temp from any subdirectory that is two levels below the root. For example, /somedir/subdir/temporary.txt is excluded.
    temp?       Exclude files and directories in the root directory whose names are a one-character extension of temp. For example, /tempa and /tempb are excluded.

Beyond Go’s filepath.Match rules, Docker also supports a special wildcard string ** that matches any number of directories (including zero). For example, **/*.go will exclude all files that end with .go that are found in all directories, including the root of the build context.

Lines starting with ! (exclamation mark) can be used to make exceptions to exclusions. The following is an example .dockerignore file that uses this mechanism:

    *.md
    !README.md

All markdown files except README.md are excluded from the context.

You can even use the .dockerignore file to exclude the Dockerfile and .dockerignore files. These files are still sent to the daemon because it needs them to do its job. But the ADD and COPY instructions do not copy them to the image.

Finally, you may want to specify which files to include in the context, rather than which to exclude. To achieve this, specify * as the first pattern, followed by one or more ! exception patterns.

#

https://daringfireball.net/projects/markdown/basics

#
