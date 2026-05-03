# How To

```clojure
(require '[clojuressh.core :as clojuressh])
```

## Exit the clojure mainline cleanly

If there are any ssh connections open apon program termination, the JVM will not exit. Make exit clean by
closing sessions after use:

```clojure
(let [session (clojuressh/ssh "remotehost" {:username "remoteusername"})]
   (try
      ;; do things with session here
      (finally
         (clojuressh.session/disconnect session))))

(shutdown-agents)
```

If you do not care about elegance and just want to force a shutdown, use `System/exit`. This will
close all connections and exit the JVM.

```clojure
(let [session (clojuressh/ssh "remotehost" {:username "remoteusername"})]
   ;; do things here
   ;; dont close
   )

(System/exit 0) ;; closes all connections and exits
```

## Debug the ssh connection process

Register an error reporting function with `clojuressh.agent/set-debug-fn` before initiating the connection

```clojure
(agent/set-debug-fn
 (fn [_level message]
   (binding [*out* *err*]
     (println message))))
```

Then when you connect you should see verbose error messages appear.

## Connect to a machine with a hard coded password

You can hard code a password in the options hash.

> **Note:** This is not recommended. You may accidentally commit your code to a repository with the password or inadvertantly expose it.

```clojure
(clojuressh/ssh "remotehost"
    {:username "remoteusername"
     :port 22
     :password "the-password"})]
```

## Connect to a machine with an encrypted private key and a hard coded passphrase

You can hard code the passphrase in the options hash.

> **Note:** This is not recommended. You may accidentally commit your code to a repository with the passphrase or inadvertantly expose it.

```clojure
(clojuressh/ssh "remotehost"
    {:username "remoteusername"
     :port 22
     :identity (str (System/getenv "HOME") "/.ssh/id_rsa")
     :passphrase "the-key-passphrase"})
```

## Turn off strict host key checking

```clojure
(clojuressh/ssh "remotehost"
    {:strict-host-key-checking false})
```

## Accept a key and add it to known hosts without complaint only on first connection

```clojure
(clojuressh/ssh "remotehost"
    {:accept-host-key :new})
```

## Accept a key if it matches a fingerprint

```clojure
(clojuressh/ssh "remotehost"
    {:accept-host-key "SHA256:/tCQlmGVCXhwqJFq3h5aiEqD1UlUD9Eg5bDwd5yF52k"})
```
## Allow connection to a legacy server that only supports RSA/SHA1 signatures

```clojure
(clojuressh/ssh "remotehost"
    {:connection-options
        {:server-host-key #(str % ",ssh-rsa")
         :client-pubkey #(str % ",ssh-rsa")}})
```

## Connect to a dropbear ssh server

A very old server:

```clojure
(clojuressh/ssh "remotehost"
    {:connection-options
        {:kex "diffie-hellman-group1-sha1"})]
```

Or perhaps:

```clojure
(clojuressh/ssh "remotehost"
    {:connection-options
        {:cipher "aes128-cbc"})]
```

## Execute a remote ssh command using authentication forwarding

```clojure
(-> (clojuressh/ssh "remotehost")
    (clojuressh/exec "ssh -o StrictHostKeyChecking=no git@github.com"
                 {:err :string
                  :agent-forwarding true})
    deref
    :err
    clojure.string/split-lines
    second)
;; => "Hi retrogradeorbit! You've successfully authenticated, but GitHub does not provide shell access.\n"
```

## Allocate a pseudo terminal for the remote shell

```clojure
(-> (clojuressh/ssh "remotehost")
    (clojuressh/exec "tty"
                 {:out :string
                  :pty true})
    deref
    :out)
;; => "/dev/pts/72\r\n"
```

## Prevent the native access warning

If you are on JDK 22+, as your terminal gets put into raw mode (for a password or passphrase prompt),
you will see a native access warning. It will probably look like this:

```
Enter Password for crispin@localhost: WARNING: A restricted method in java.lang.System has been called
WARNING: java.lang.System::load has been called by com.sun.jna.Native in an unnamed module (file:/home/crispin/.m2/repository/net/java/dev/jna/jna/5.14.0/jna-5.14.0.jar)
WARNING: Use --enable-native-access=ALL-UNNAMED to avoid a warning for callers in this module
WARNING: Restricted methods will be blocked in a future release unless native access is enabled
```

You either print this warning early by calling `clojuressh.terminal/get-width`

```
(clojuressh.terminal/get-width) ;; prints native access warning when first called
```

You can silence the message completely by starting the JVM with `--enable-native-access=ALL-UNNAMED`.
Pick whichever of these matches your launcher:

**`deps.edn`** — add to the alias (or top-level) that runs your app:

```clojure
{:aliases
 {:run {:main-opts ["-m" "my.app"]
        :jvm-opts  ["--enable-native-access=ALL-UNNAMED"]}}}
```

**Leiningen `project.clj`**:

```clojure
:jvm-opts ["--enable-native-access=ALL-UNNAMED"]
```

**Plain `java`** (e.g. running an uberjar):

```
java --enable-native-access=ALL-UNNAMED -jar my-app.jar
```

**Environment variable** (applies to every JVM spawned in the shell):

```
export JDK_JAVA_OPTIONS=--enable-native-access=ALL-UNNAMED
```

No flag is required on JDK 21 and earlier. If you hit an
`IllegalCallerException` about native access on a JDK where the warning
has become a hard error, `clojuressh` will rethrow it wrapped in an
`ex-info` pointing you at this section.
