package CommitRater;
use Moose;
use feature    qw(fc state);
use List::Util qw(all any);
use Lingua::EN::Tagger;


has repo => (
    is       => 'ro',
    isa      => 'CommitRater::Repo',
    required => 1,
);


use constant RULES => qw(
    empty_second_line
    subject_limit
    capitalize_subject
    no_period_subject
    imperative_subject
    body_limit
    body_used
);


sub rate
{
    my ($self) = @_;
    $self->repo->update;

    my %results;
    $self->repo->each_commit(sub { rate_commit($_, \%results) });
    return \%results
}


sub default_result { map { $_ => {pass => 0, fail => 0, undef => 0} } RULES }

sub rate_commit
{
    my ($commit, $results) = @_;

    my $author = $results->{fc $commit->{email}} //= {default_result};
    my $rules  = rate_message(@{$commit->{message}});

    while (my ($k, $v) = each %$rules)
    {
        my $what = $v ? 'pass' : defined $v ? 'fail' : 'undef';
        $author->{$k}{$what} += 1;
    }
}


sub rate_message
{
    no warnings 'uninitialized';
    state $tagger = Lingua::EN::Tagger->new;
    my ($subject, @body) = @_;

    my %result;
    @result{(RULES)} = (
        undef,                                           # empty_second_line
        length $subject <= 50,                           # subject_limit
        scalar($subject =~ /^\p{Uppercase}/),            # capitalize_subject
        scalar($subject !~ /\.\s*$/),                    # no_period_subject
        scalar($tagger->add_tags($subject) =~ /<vbp?>/), # imperative subject
        undef,                                           # body limit
        scalar(any { /\S/ } @body),                      # body used
    );

    if ($result{body_used})
    {
        $result{empty_second_line} = !length $body[0];
        $result{    body_limit   } = all { length $_ <= 72 } @body;
    }

    return \%result
}


no Moose;
1
