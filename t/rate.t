use Test::Most;
use File::Temp;
use Git::Repository;
use CommitRater;


my  $test_dir  = File::Temp->newdir;
my $commit_dir = "$test_dir/local";
my $clone_dir  = "$test_dir/clone";


Git::Repository->run(init => $commit_dir);
my $local = Git::Repository->new(work_tree => $commit_dir);


sub commit
{
    my ($email, $message) = @_;
    $email =~ /([^@]+)/;
    $local->run(qw(config --local user.name),  $1);
    $local->run(qw(config --local user.email), $email);
    $local->run(qw(commit --allow-empty --allow-empty-message -qm), $message);
}

commit 'dev@elo.per', <<'END_OF_MESSAGE';
added implementation for frobnication interval retrieval.
END_OF_MESSAGE

commit 'dev@elo.per', <<'END_OF_MESSAGE';
Add cock-nication interval retrieval

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


my $repo  = CommitRater::Repo->new(local => $clone_dir, remote => $commit_dir);


sub result
{
    my ($pass, $fail, $undef) = @_;
    return {
        pass  => $pass,
        fail  => $fail,
        undef => $undef,
    }
}

my $expected = {
    'dev@elo.per' => {
        empty_second_line  => result(1, 1, 1),
        subject_limit      => result(2, 1, 0),
        capitalize_subject => result(2, 1, 0),
        no_period_subject  => result(2, 1, 0),
        imperative_subject => result(1, 2, 0),
        body_limit         => result(1, 1, 1),
        body_used          => result(2, 1, 0),

        no_short_message   => result(2, 1, 0),
        no_long_message    => result(3, 0, 0),
        no_bulk_change     => result(0, 3, 0),
        no_vulgarity       => result(2, 1, 0),
        no_misspelling     => result(1, 2, 0),
        no_duplicate       => result(3, 0, 0),
    },
    'j@i.m'       => {
        empty_second_line  => result(0, 0, 1),
        subject_limit      => result(1, 0, 0),
        capitalize_subject => result(0, 1, 0),
        no_period_subject  => result(1, 0, 0),
        imperative_subject => result(0, 1, 0),
        body_limit         => result(0, 0, 1),
        body_used          => result(0, 1, 0),

        no_short_message   => result(0, 1, 0),
        no_long_message    => result(1, 0, 0),
        no_bulk_change     => result(0, 1, 0),
        no_vulgarity       => result(1, 0, 0),
        no_misspelling     => result(1, 0, 0),
        no_duplicate       => result(1, 0, 0),
    },
};

is_deeply(CommitRater->new(repo => $repo)->rate, $expected,
          'rate aggregates expected results');


is_deeply(CommitRater->new(repo => $repo)->rate(2), {
    'dev@elo.per' => {
        empty_second_line  => result(0, 1, 0),
        subject_limit      => result(1, 0, 0),
        capitalize_subject => result(1, 0, 0),
        no_period_subject  => result(1, 0, 0),
        imperative_subject => result(0, 1, 0),
        body_limit         => result(0, 1, 0),
        body_used          => result(1, 0, 0),

        no_short_message   => result(0, 1, 0),
        no_long_message    => result(1, 0, 0),
        no_bulk_change     => result(0, 1, 0),
        no_vulgarity       => result(1, 0, 0),
        no_misspelling     => result(1, 0, 0),
        no_duplicate       => result(1, 0, 0),
    },
    'j@i.m'       => {
        empty_second_line  => result(0, 0, 1),
        subject_limit      => result(1, 0, 0),
        capitalize_subject => result(0, 1, 0),
        no_period_subject  => result(1, 0, 0),
        imperative_subject => result(0, 1, 0),
        body_limit         => result(0, 0, 1),
        body_used          => result(0, 1, 0),

        no_short_message   => result(0, 1, 0),
        no_long_message    => result(1, 0, 0),
        no_bulk_change     => result(0, 1, 0),
        no_vulgarity       => result(1, 0, 0),
        no_misspelling     => result(1, 0, 0),
        no_duplicate       => result(1, 0, 0),
    },
}, 'rate with limit handles only recent commits');

is_deeply(CommitRater->new(repo => $repo)->rate(10000), $expected,
          'limit larger than number of commits aggregates all commits');


done_testing
