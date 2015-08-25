$script = <<SCRIPT
apt-get update
apt-get install -y carton perl-doc git libaspell-dev
git config --global user.name "vagrant"
git config --global user.email "vagrant@vagrant.vg"
cd /vagrant
make install
locale-gen "de_DE.UTF-8"
dpkg-reconfigure locales
SCRIPT


Vagrant.configure(2) do |config|
  config.vm.box = "ubuntu/trusty64"
  config.vm.provision "shell", inline: $script
end
