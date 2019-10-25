![image](https://user-images.githubusercontent.com/30617834/67572267-61d2c680-f792-11e9-8316-cb1e18283705.png)
# Packet Browser

Packet Browser is a simple Java Swing application that allows trace files containing packet data to be opened and edited in a spreadsheet-like interface.

## Features

Beyond the given specifications for this assignment, a few things were added:

* The ability to look at packet data flow from/to/between particular host ips or ports
* The ability to print the table
* A tabbed interface design allowing multiple files to be opened and edited at once
* Usage of threading via SwingWorker to perform time-intensive processes (e.g. loading large files) without freezing the GUI
* Intelligent copy and paste of table data
* Filtering of what columns are displayed in the table