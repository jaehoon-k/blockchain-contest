const EVBatteryToken = artifacts.require("EVBatteryToken");

const _name = "EVBatteryToken";
const _symbol = "EBT";
const _decimals = 18;

module.exports = function(deployer) {
  deployer.deploy(EVBatteryToken, _name, _symbol, _decimals);
};

