const batteryCertificates = artifacts.require("BatteryCertificates.sol");
// const batteryCertificates = artifacts.require("BatteryCertificates.sol");

module.exports = function(deployer) {
  deployer.deploy(batteryCertificates, "Battery Certificates", "BCT");
};
