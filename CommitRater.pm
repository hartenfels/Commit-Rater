package CommitRater;
use Moose;


has repo => (
    is       => 'ro',
    isa      => 'CommitRater::Repo',
    required => 1,
);


sub rate
{
    my ($self) = @_;
    $self->repo->update;
    print $self->repo->map_commits(sub { "$_->{sha}\n" });
}


no Moose;
1
