use strict;
use warnings;
use LWP::UserAgent;
use JSON;
use Config::Tiny;

my @lines = <STDIN>;

my $config = Config::Tiny->read('config.ini');
my $email = $$config{_}{email};
my $pass =  $$config{_}{pass};

my ($title, @content) = @lines;

sub send_to_remote {
  my $uri = 'http://localhost:9000/entry/remoteSave';

  my $json = {title => $title, content => join('', @content), rating => 1};

  my $req = HTTP::Request->new('POST', $uri);
  $req->header('Content-Type' => 'application/json');
  $req->content(JSON->new->encode($json));
  $req->authorization_basic($email, $pass);

  my $lwp = LWP::UserAgent->new;
  $lwp->request($req);
}

if(@content){
    send_to_remote();
}

