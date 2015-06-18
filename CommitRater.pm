package CommitRater;
use Moose;
use List::Util qw(all any);
use Lingua::EN::Tagger;


has repo => (
    is       => 'ro',
    isa      => 'CommitRater::Repo',
    required => 1,
);

our $tagger = Lingua::EN::Tagger->new;


sub rate
{
    my ($self) = @_;
    $self->repo->update;
    $self->repo->each_commit(sub
    {
        my $seven = rate_commit(@{$_->{message}});
    });
}


sub rate_message
{
    no warnings 'uninitialized';
    my ($subject, @body) = @_;

    my %result = (
        empty_second_line  => undef,
        subject_limit      => length $subject <= 50,
        capitalize_subject => scalar($subject =~ /^\p{Uppercase}/),
        no_period_subject  => scalar($subject !~ /\.\s*$/),
        imperative_subject => scalar($tagger->add_tags($subject) =~ /<vbp?>/),
        body_limit         => undef,
        body_used          => scalar(any { /\S/ } @body),
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
