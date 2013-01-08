-module(module).
-export([one/1]).

one(Arg) when is_integer(Arg) ->
    Arg + 1;
one(Arg) ->
    Arg.