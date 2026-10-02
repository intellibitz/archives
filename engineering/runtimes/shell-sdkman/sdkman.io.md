# https://sdkman.io/

    https://github.com/sdkman/sdkman-cli

# https://sdkman.io/install

Go on, paste and run the following in a terminal:

    curl -s "https://get.sdkman.io" | bash

Configuration can be found in the ~/.sdkman/etc/config file. To edit the configuration, the sdk config command may be issued to edit this file in the system editor. The following configurations are available:

    # make sdkman non-interactive, preferred for CI environments
    sdkman_auto_answer=true|false

    # check for newer versions and prompt for update
    sdkman_selfupdate_feature=true|false

    # disables SSL certificate verification
    # https://github.com/sdkman/sdkman-cli/issues/327
    # HERE BE DRAGONS....
    sdkman_insecure_ssl=true|false

    # configure curl timeouts
    sdkman_curl_connect_timeout=5
    sdkman_curl_continue=true
    sdkman_curl_max_time=10

    # subscribe to the beta channel
    sdkman_beta_channel=true|false

    # enable verbose debugging
    sdkman_debug_mode=true|false

    # enable colour mode
    sdkman_colour_enable=true|false

    # enable automatic env
    sdkman_auto_env=true|false

    # enable bash or zsh auto-completion
    sdkman_auto_complete=true|false

    sdk help
    sdk list
    sdk install java
    sdk current

Please open a new terminal, or run the following in the existing one:

    source "$HOME/.sdkman/bin/sdkman-init.sh"

Then issue the following command:

    sdk version
    sdk help

# https://sdkman.io/usage

    sdk install java
    sdk install scala 3.4.2
    sdk install java 17-zulu /Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home

    sdk uninstall scala 3.4.2
    sdk list
    sdk list groovy
    sdk use scala 3.4.2
    sdk default scala 3.4.2
    sdk current java

    sdk env init

A config file with the following content has now been created in the current directory:

    # Enable auto-env through the sdkman_auto_env config
    # Add key=value pairs of SDKs to use below
    java=21.0.4-tem

The file is pre-populated with the current JDK version in use, but can contain as many key-value pairs of supported SDKs as needed. To switch to the configuration present in your .sdkmanrc file, simply issue the following command:

    sdk env

You should see output that looks something like this:

    Using java version 21.0.4-tem in this shell.

Your path has now also been updated to use any of these SDKs in your current shell. When leaving a project, you may want to reset the SDKs to their default version. This can be achieved by entering:

    sdk env clear

    Restored java version to 21.0.4-tem (default)

After checking out a new project, you may be missing some SDKs specified in the project's .sdkmanrc file. To install these missing SDKs, just type:

    sdk env install

    sdk upgrade springboot

    sdk offline enable
    Forced offline mode enabled.

    sdk offline disable
    Online mode re-enabled!

    sdk selfupdate

    sdk flush

    sdk home java 21.0.4-tem
    /home/myuser/.sdkman/candidates/java/21.0.4-tem

# https://sdkman.io/jdks

    sdk install java x.y.z-amzn   #https://aws.amazon.com/corretto/
    sdk install java x.y.z-oracle #https://www.oracle.com/java/
    sdk install java x.y.z-open   #https://jdk.java.net/
    sdk install java x.y.z-tem    #https://projects.eclipse.org/projects/adoptium.temurin

# https://sdkman.io/sdks

    sdk install gradle          #http://gradle.org/
    sdk install gradleprofiler  #https://github.com/gradle/gradle-profiler
    sdk install http4k          #http://http4k.org/
    sdk install kotlin          #https://kotlinlang.org/
    sdk install kscript         #https://github.com/holgerbrandl/kscript
    sdk install ktx             #https://github.com/mpetuska/ktx
    sdk install tomcat          #https://tomcat.apache.org/

# ~$ curl -s "https://get.sdkman.io" | bash

                                -+syyyyyyys:
                            `/yho:`       -yd.
                         `/yh/`             +m.
                       .oho.                 hy                          .`
                     .sh/`                   :N`                `-/o`  `+dyyo:.
                   .yh:`                     `M-          `-/osysoym  :hs` `-+sys:      hhyssssssssy+
                 .sh:`                       `N:          ms/-``  yy.yh-      -hy.    `.N-````````+N.
               `od/`                         `N-       -/oM-      ddd+`     `sd:     hNNm        -N:
              :do`                           .M.       dMMM-     `ms.      /d+`     `NMMs       `do
            .yy-                             :N`    ```mMMM.      -      -hy.       /MMM:       yh
          `+d+`           `:/oo/`       `-/osyh/ossssssdNMM`           .sh:         yMMN`      /m.
         -dh-           :ymNMMMMy  `-/shmNm-`:N/-.``   `.sN            /N-         `NMMy      .m/
       `oNs`          -hysosmMMMMydmNmds+-.:ohm           :             sd`        :MMM/      yy
      .hN+           /d:    -MMMmhs/-.`   .MMMh   .ss+-                 `yy`       sMMN`     :N.
     :mN/           `N/     `o/-`         :MMMo   +MMMN-         .`      `ds       mMMh      do
    /NN/            `N+....--:/+oooosooo+:sMMM:   hMMMM:        `my       .m+     -MMM+     :N.

/NMo -+ooooo+/:-....`...:+hNMN.  `NMMMd`        .MM/       -m:    oMMN.     hs
-NMd` :mm -MMMm- .s/ -MMm. /m- mMMd -N.
`mMM/                                      .-   /MMh. -dMo     -MMMy        od. .MMMs..---yh
+MMM.                                           sNo`.sNMM+ :MMMM/ sh`+MMMNmNm+++-
mMMM-                                           /--ohmMMM+     :MMMMm.       `hyymmmdddo
MMMMh. ```                  `-+yy/`yMMM/     :MMMMMy       -sm:.``..-:-.`
dMMMMmo-.``````..-:/osyhddddho.           `+shdh+.   hMMM:     :MmMMMM/   ./yy/` `:sys+/+sh/
.dMMMMMMmdddddmmNMMMNNNNNMMMMMs           sNdo-      dMMM-  `-/yd/MMMMm-:sy+.   :hs-      /N`
`/ymNNNNNNNmmdys+/::----/dMMm:          +m-         mMMM+ohmo/.` sMMMMdo-    .om:       `sh
`.-----+/.`       `.-+hh/`         `od.          NMMNmds/     `mmy:`     +mMy      `:yy.
/moyso+//+ossso:.           .yy`          `dy+:`         ..       :MMMN+---/oys:
/+m:  `.-:::-`               /d+                                    +MMMMMMMNh:`
+MN/                        -yh.                                     `+hddhy+.
/MM+                       .sh:
:NMo                      -sh/
-NMs                    `/yy:
.NMy                  `:sh+.
`mMm`               ./yds-
`dMMMmyo:-.```.-:oymNy:`
+NMMMMMMMMMMMMMMMMms:`
-+shmNMMMNmdy+:`

                                                                 Now attempting installation...

Looking for a previous installation of SDKMAN...
Looking for unzip...
Looking for zip...
Looking for curl...
Looking for sed...
Installing SDKMAN scripts...
Create distribution directories...
Getting available candidates...
Prime the config file...
Installing script cli archive...

- Downloading...
  ######################################################################## 100.0%
- Checking archive integrity...
- Extracting archive...
- Copying archive contents...
- Cleaning up...

- Downloading...
  ######################################################################## 100.0%
- Checking archive integrity...
- Extracting archive...
- Copying archive contents...
- Cleaning up...

Set version to 5.18.1 ...
Set native version to 0.2.2 ...
Attempt update of interactive bash profile on regular UNIX...
Added sdkman init snippet to /home/zbook/.bashrc
Attempt update of zsh profile...
Updated existing /home/zbook/.zshrc

All done!

You are subscribed to the STABLE channel.

Enjoy!!!

#
