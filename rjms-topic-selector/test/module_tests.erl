-module(module_tests).
-include_lib("eunit/include/eunit.hrl").

one_success_test() ->
    ?assertMatch(2, module:one(1)).

two_success_test() ->
    ?assertMatch(ok, module:one(ok)).