# Plugin Importer+Exporter
<!-- Plugin description -->
  This plugin currently has these main features.

- Dump which plugins are installed to a JSON file
- Dump which plugins are installed as plugin repository
- Download all plugins from a JSON file


[GitHub](https://github.com/shiraji/plugin-importer-exporter) | [Issues](https://github.com/shiraji/plugin-importer-exporter/issues)
<!-- Plugin description end -->
## Installation

Use the IDE's plugin manager to install the latest version of the plugin.

## Usage
### Export to JSON file

* Go to `File > Export Plugins... > select JSON file to write > Export`

### Import from JSON file

* Go to `File > Import Plugins... > select JSON file to read > Install Plugins`
* Restart!

### Options

**Save Disabled Plugin Information**

By default, this plugin ignore following plugins to export:

* IDEA CORE (com.intellij)
* This plugin
* Bundled plugins
* Disbaled plugins

But, if you check this option, the plugin exports disbaled plugins include disabled bundle plugins.

## Contributing

1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Submit a pull request

## TODO

1. Ignore unpublished plugins to export
1. Import/Export plugins settings
1. Auto export
1. Check capability of installing the plugins (e.g. AndroidStduio specific plugins should not be install to PhpStorm)
1. i18n
1. Dump plugin information in different format
1. Install specific version of plugins
1. Tests

## License

```
Copyright 2015 Yoshinori Isogai

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
