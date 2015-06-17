package CommitRater::Git::Repository;
use strict;
use warnings;
use Git::Repository;
use base qw(Git::Repository);

sub git { shift->run(@_, {fatal => '!0'}) }

1
