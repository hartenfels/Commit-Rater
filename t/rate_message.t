use Test::Most;
use CommitRater;


sub rate_ok
{
    my ($name, $expected, $message) = @_;

    my @lines = $message =~ "\n" ? split "\n", $message : ($message);
    chomp for @lines;

    my $got = CommitRater::rate_message(@lines);
    $got->{$_} = $got->{$_} ? 1 : defined $got->{$_} ? 0 : undef for keys %$got;

    is_deeply $got, $expected, $name;
}


rate_ok 'empty commit message', {
    empty_second_line  => undef,
    subject_limit      => 1,
    capitalize_subject => 0,
    no_period_subject  => 1,
    imperative_subject => 0,
    body_limit         => undef,
    body_used          => 0,
}, '';


rate_ok 'commit message that does it all wrong', {
    empty_second_line  => undef,
    subject_limit      => 0,
    capitalize_subject => 0,
    no_period_subject  => 0,
    imperative_subject => 0,
    body_limit         => undef,
    body_used          => 0,
}, <<END_OF_MESSAGE;
added implementation for frobnication interval retrieval.
END_OF_MESSAGE


rate_ok 'commit message that does it all right', {
    empty_second_line  => 1,
    subject_limit      => 1,
    capitalize_subject => 1,
    no_period_subject  => 1,
    imperative_subject => 1,
    body_limit         => 1,
    body_used          => 1,
}, <<END_OF_MESSAGE;
Add frobnication interval retrieval

There used to be no way for clients to retrieve the widget's rate of
frobnication. This commit adds a get_frobnication_interval function that
should alleviate these issues.
END_OF_MESSAGE


rate_ok 'commit message in the middle', {
    empty_second_line  => 0,
    subject_limit      => 1,
    capitalize_subject => 1,
    no_period_subject  => 1,
    imperative_subject => 0,
    body_limit         => 1,
    body_used          => 1,
}, <<END_OF_MESSAGE;
Bug
This fixes bug good.
END_OF_MESSAGE


rate_ok 'whitespace and empty lines do not count as body', {
    empty_second_line  => undef,
    subject_limit      => 1,
    capitalize_subject => 1,
    no_period_subject  => 1,
    imperative_subject => 1,
    body_limit         => undef,
    body_used          => 0,
}, <<END_OF_MESSAGE;
Fail at fooling rate_commit
   
	    

    
END_OF_MESSAGE


done_testing
