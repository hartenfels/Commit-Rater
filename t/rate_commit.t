use Test::Most;
use CommitRater;


my @commits;
sub commit
{
    my ($email, $message) = @_;

    my @lines = $message =~ "\n" ? split "\n", $message : ($message);
    chomp for @lines;

    push @commits, {
        email   => $email,
        message => \@lines,
    }
}

commit 'dev@elo.per', <<'END_OF_MESSAGE';
added implementation for frobnication interval retrieval.
END_OF_MESSAGE

commit 'dev@elo.per', <<'END_OF_MESSAGE';
Add frobnication interval retrieval

There used to be no way for clients to retrieve the widget's rate of
frobnication. This commit adds a get_frobnication_interval function that
should alleviate these issues.
END_OF_MESSAGE

commit 'dev@elo.per', <<'END_OF_MESSAGE';
Bug
This fixes that super nasty bug that came up in the last meeting for real now
END_OF_MESSAGE

commit 'j@i.m', <<'END_OF_MESSAGE';
END_OF_MESSAGE


my %results;
CommitRater::rate_commit($_, \%results) for @commits;

sub result
{
    my ($pass, $fail, $undef) = @_;
    return {
        pass  => $pass,
        fail  => $fail,
        undef => $undef,
    }
}

is_deeply \%results, {
    'dev@elo.per' => {
        empty_second_line  => result(1, 1, 1),
        subject_limit      => result(2, 1, 0),
        capitalize_subject => result(2, 1, 0),
        no_period_subject  => result(2, 1, 0),
        imperative_subject => result(1, 2, 0),
        body_limit         => result(1, 1, 1),
        body_used          => result(2, 1, 0),
    },
    'j@i.m'       => {
        empty_second_line  => result(0, 0, 1),
        subject_limit      => result(1, 0, 0),
        capitalize_subject => result(0, 1, 0),
        no_period_subject  => result(1, 0, 0),
        imperative_subject => result(0, 1, 0),
        body_limit         => result(0, 0, 1),
        body_used          => result(0, 1, 0),
    },
}, 'rate_commit aggregates expected results';


done_testing
