# Changelog

## [0.1.2](https://github.com/nasenov/miscale2pg/compare/0.1.1...0.1.2) (2026-07-24)


### Features

* add grafana dashboard ([6f72c44](https://github.com/nasenov/miscale2pg/commit/6f72c44cdcc128adc595ccd46f680f188107ca95))


### Miscellaneous Chores

* store grafana dashboard in json ([58ca28e](https://github.com/nasenov/miscale2pg/commit/58ca28eac84b4c474c977dfddc41d085467b162a))
* store grafana dashboard in v1 json format ([8dc6366](https://github.com/nasenov/miscale2pg/commit/8dc636603c272cd2356381c0b891fab85634115c))
* update grafana dashboard ([bc36062](https://github.com/nasenov/miscale2pg/commit/bc360622750ad4a286629a1550a82625c4f6a872))
* update grafana dashboard ([a050c47](https://github.com/nasenov/miscale2pg/commit/a050c47dbebf63eb0840d144df559d118ea16d3a))
* update grafana dashboard ([25a4dd5](https://github.com/nasenov/miscale2pg/commit/25a4dd58c2c0b3a73da7a6887e8d118f0278d5f2))

## [0.1.1](https://github.com/nasenov/miscale2pg/compare/0.1.0...0.1.1) (2026-07-15)


### Bug Fixes

* graalvm native image ([1cc8eaf](https://github.com/nasenov/miscale2pg/commit/1cc8eafc48e2e7b1dc6051cfaecdb43b60b7acea))

## [0.1.0](https://github.com/nasenov/miscale2pg/compare/0.0.10...0.1.0) (2026-07-15)


### ⚠ BREAKING CHANGES

* save measurements in transaction

### Features

* add measurement table constraints ([93a8b32](https://github.com/nasenov/miscale2pg/commit/93a8b32d8420ea0a08e0917fff6e7f717452c0ea))
* add mi scale measurement validations ([baedae4](https://github.com/nasenov/miscale2pg/commit/baedae4e6997c66cf076cc387883196cb41f82f9))
* get measurements by time range endpoint ([ffb2c0f](https://github.com/nasenov/miscale2pg/commit/ffb2c0f7a761bc26eda85ec807da930b81fa3781))
* save measurements in transaction ([a2caacc](https://github.com/nasenov/miscale2pg/commit/a2caaccc79a8542c4fc2df66f16bed290ad5902e))


### Bug Fixes

* **deps:** update plugin org.graalvm.buildtools.native (1.1.3 ➔ 1.1.4) ([#29](https://github.com/nasenov/miscale2pg/issues/29)) ([9b2d89e](https://github.com/nasenov/miscale2pg/commit/9b2d89eb9969ab6883a9dc7fb592ecdf04492562))


### Miscellaneous Chores

* add validation for time to be in the past ([5f0d077](https://github.com/nasenov/miscale2pg/commit/5f0d0779f246e7781c5e1f6ccc083e7ceb11ee5f))
* fix deprecated usage of @Valid ([0beb3da](https://github.com/nasenov/miscale2pg/commit/0beb3daa05890984f95a69f7859ee86a955e080a))
* handle IOException in controller upload method ([e1ffa0a](https://github.com/nasenov/miscale2pg/commit/e1ffa0a73a46b40fcdd680fa25262769d2092330))


### Code Refactoring

* remove unused save method return value ([1765c92](https://github.com/nasenov/miscale2pg/commit/1765c928a74cadcbc202d71662cfbd2f39099b84))

## [0.0.10](https://github.com/nasenov/miscale2pg/compare/0.0.9...0.0.10) (2026-07-07)


### Bug Fixes

* add native hint for measurement model ([fe995e7](https://github.com/nasenov/miscale2pg/commit/fe995e79b52a5a2b9399cbbd4f5b9698b639823a))

## [0.0.9](https://github.com/nasenov/miscale2pg/compare/0.0.8...0.0.9) (2026-07-07)


### Performance Improvements

* enable virtual threads ([5863947](https://github.com/nasenov/miscale2pg/commit/5863947d45bff053985d03f8b9d5b7daebb35cd1))


### Miscellaneous Chores

* fix warning ([e66b02a](https://github.com/nasenov/miscale2pg/commit/e66b02aa82b6ea04aea020b640e64b166d9ba15a))


### Code Refactoring

* use spring boot starter jdbc ([8a5d97f](https://github.com/nasenov/miscale2pg/commit/8a5d97f773b4b7f4bd59a0cf196d010e5ce7862b))

## [0.0.8](https://github.com/nasenov/miscale2pg/compare/0.0.7...0.0.8) (2026-07-06)


### Features

* add upload response ([aedbdd5](https://github.com/nasenov/miscale2pg/commit/aedbdd5c0524ca24034f5b53a91b71b913d3bbb1))
* enable problem details response for bad requests ([68513b1](https://github.com/nasenov/miscale2pg/commit/68513b10a02f7ae3898948827a0f74f78dc8d4a8))


### Bug Fixes

* **deps:** update plugin org.graalvm.buildtools.native (1.1.2 ➔ 1.1.3) ([#20](https://github.com/nasenov/miscale2pg/issues/20)) ([b1d4bda](https://github.com/nasenov/miscale2pg/commit/b1d4bdaafda0e5552c24ad6455680b2c8f0163df))
* handle empty file upload ([356bb95](https://github.com/nasenov/miscale2pg/commit/356bb95a5e3c744142673247207660630738f476))
* handle non csv file upload ([89dfe23](https://github.com/nasenov/miscale2pg/commit/89dfe234802181b8578c604f70904f944e7b7d2e))


### Miscellaneous Chores

* fix typo ([15df6ea](https://github.com/nasenov/miscale2pg/commit/15df6eaf87358ca2aa3b139d7612276fbbe095fc))


### Code Refactoring

* local development setup ([a94cf95](https://github.com/nasenov/miscale2pg/commit/a94cf9567d1835a2a0a04766f130e92c8231b95e))
* read csv file in controller ([ba2c71d](https://github.com/nasenov/miscale2pg/commit/ba2c71d395bda1b0b7a6a12a9a4acbb64518f185))

## [0.0.7](https://github.com/nasenov/miscale2pg/compare/0.0.6...0.0.7) (2026-06-22)


### Performance Improvements

* use graalvm native image ([ebc84d7](https://github.com/nasenov/miscale2pg/commit/ebc84d778887fd2e7cc1f7bdd422f8714c13cc08))

## [0.0.6](https://github.com/nasenov/miscale2pg/compare/0.0.5...0.0.6) (2026-06-22)


### Bug Fixes

* enable spring aot during OCI image build ([af53319](https://github.com/nasenov/miscale2pg/commit/af5331979887823bdef96ad797f2ef1e58efd8cc))

## [0.0.5](https://github.com/nasenov/miscale2pg/compare/0.0.4...0.0.5) (2026-06-22)


### Performance Improvements

* enable spring aot ([914af27](https://github.com/nasenov/miscale2pg/commit/914af2714eaae08891c0963003cc76279540f93f))

## [0.0.4](https://github.com/nasenov/miscale2pg/compare/0.0.3...0.0.4) (2026-06-22)


### Performance Improvements

* use spring-data-jdbc instead of spring-data-jpa ([d0df2dc](https://github.com/nasenov/miscale2pg/commit/d0df2dcb874027559e3aa74b5129c3a8a885a31e))


### Miscellaneous Chores

* auto update version with release-please ([2d6fd4e](https://github.com/nasenov/miscale2pg/commit/2d6fd4e64bc58b2208120f290023c1b4f89533d5))
* let renovate find changelog ([429f4d6](https://github.com/nasenov/miscale2pg/commit/429f4d6e55ea8db21972b958986c857506da021d))

## [0.0.3](https://github.com/nasenov/miscale2pg/compare/0.0.2...0.0.3) (2026-06-21)


### Features

* enable prometheus metrics ([9b6282a](https://github.com/nasenov/miscale2pg/commit/9b6282aff1dcf8bfde9eba11c24028a2ab7d8a2e))


### Miscellaneous Chores

* prepare release ([a97dc3b](https://github.com/nasenov/miscale2pg/commit/a97dc3b6dbcb9e3c2727e1b198800aeee0c4dae5))

## [0.0.2](https://github.com/nasenov/miscale2pg/compare/0.0.1...0.0.2) (2026-06-21)


### Miscellaneous Chores

* add actuator health endpoints ([aa8d7d1](https://github.com/nasenov/miscale2pg/commit/aa8d7d1b11b25883fad2a7c98c18a92453eb0da7))
* prepare release ([4740c72](https://github.com/nasenov/miscale2pg/commit/4740c7223dc3150fe02381f8655b0da55be55d26))
