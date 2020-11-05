const erc721 = artifacts.require("ERC721");

module.exports = function(deployer) {
  deployer.deploy(erc721);
};
