use strict;
use warnings;
use LWP::UserAgent;
use JSON;
use Config::Tiny;

my @lines = <STDIN>;

my $config = Config::Tiny->read('config.ini');
my $email = $$config{_}{email};
my $pass =  $$config{_}{pass};

my ($headword, @context) = @lines;

sub send_to_remote {
  my $uri = 'http://localhost:9000/entry/remoteSave';

  my $json = {headword => $headword, context => join('', @context), rating => 1};

  my $req = HTTP::Request->new('POST', $uri);
  $req->header('Content-Type' => 'application/json');
  $req->context(JSON->new->encode($json));
  $req->authorization_basic($email, $pass);

  my $lwp = LWP::UserAgent->new;
  $lwp->request($req);
}

if(@context){
    send_to_remote();
}

