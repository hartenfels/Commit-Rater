package CommitRater;
use Moose;
use feature    qw(fc state);
use List::Util qw(all any);
use Lingua::EN::Tagger;
use CommitRater::Repo;


has repo => (
    is       => 'ro',
    isa      => 'Maybe[CommitRater::Repo]',
    required => 1,
);

has dups => (
    is      => 'ro',
    isa     => 'HashRef',
    default => sub { {} },
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

use constant NONOS => qw(
    no_short_message
    no_long_message
    no_bulk_change
    no_vulgarity
    no_misspelling
    no_duplicate
);

use constant ALL_KEYS => (RULES, NONOS);


sub rate
{
    my ($self, $limit) = @_;
    $self->repo->update;

    my %results;
    $self->repo->each_commit(sub { $self->rate_commit($_, \%results) }, $limit);
    return \%results
}


sub default_result { map { $_ => {pass => 0, fail => 0, undef => 0} } ALL_KEYS }

sub rate_commit
{
    my ($self, $commit, $results) = @_;

    my $author = $results->{fc $commit->{email}} //= {default_result};
    my $rules  = $self->rate_message(@{$commit->{message}});

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
    my ($self, $subject, @body) = @_;
    my  $words           = split ' ', $subject;

    my %result;
    @result{(ALL_KEYS)} = (
        undef,                                           # empty_second_line
        length $subject <= 50,                           # subject_limit
        scalar($subject =~ /^\p{Uppercase}/),            # capitalize_subject
        scalar($subject !~ /\.\s*$/),                    # no_period_subject
        scalar($tagger->add_tags($subject) =~ /<vbp?>/), # imperative subject
        undef,                                           # body_limit
        scalar(any { /\S/ } @body),                      # body used

        $words >  2,                                     # no_short_message
        $words < 10,                                     # no_long_message
        0,                                               # no_bulk_change
        0,                                               # no_vulgarity
        0,                                               # no_misspelling
        !exists($self->dups->{$subject}),                # no_duplicate
    );

    if ($result{body_used})
    {
        $result{empty_second_line} = !length $body[0];
        $result{    body_limit   } = all { length $_ <= 72 } @body;
    }

    $self->dups->{$subject} = 1;

    return \%result
}


no Moose;
1
