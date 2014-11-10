### `rjms` and the `rjms-bin` directory

The `rjms-bin` directory (in `rabbit-jms-client`) is a set of utilities for
managing the Rabbitmq JMS project repositories in the directory `rjms`.

The `rjms` directory should contain all the git repositories of the project as
immediate sub-directories.

The utilities here include:

* `bump-release` - realise the next tagged release and bump versions for the next snapshot.
* `clean-all` - issue `mvn clean` in all Maven repositories under `rjms`.
* `run-in-git` - run a single command in each git repository undeer `rjms`.
* `update-rjms-versions` - update the files in the `rjms` repositories that record the release number.
* `update-tag` - utility for `update-rjms-versions` that performs careful checks before updating.
* `update-versions.txt` - list of parametrized `update-tag` commands driven by `update-rjms-versions`.

Before use, the files in this directory should be copied to `$HOME/dev/rjms/bin` and executed
by `./bin/util ...` in the `rjms` directory.
