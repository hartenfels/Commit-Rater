package CommitRater::Repo;
use Moose;
use File::Path qw(remove_tree);
use CommitRater::Git::Repository;


has remote => (
    is       => 'ro',
    isa      => 'Str',
    required => 1,
);

has local => (
    is       => 'ro',
    isa      => 'Str',
    required => 1,
);

has repo => (
    is      => 'ro',
    isa     => 'CommitRater::Git::Repository',
    lazy    => 1,
    clearer => 'clear_repo',
    default => sub
    {   CommitRater::Git::Repository->new(work_tree => shift->local) },
);


sub clone
{
    my ($self) = @_;
    CommitRater::Git::Repository->git(qw(clone -nq), $self->remote,
                                                     $self->local);
}

sub pull
{
    my ($self) = @_;
    $self->repo->git('pull');
}

sub update
{
    my ($self) = @_;

    my $local = $self->local;
    if (-e $local)
    {
        # try to pull
        eval { return $self->pull };
        # didn't work, delete the repo and re-clone
        $self->clear_repo;
        remove_tree($local);
        die "Pull failed and can't remove '$local' to re-clone\n" if -e $local;
    }

    $self->clone;
}


no Moose;
1
