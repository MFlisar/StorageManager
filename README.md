# ONLY PREPARING FOR FIRST RELEASE - REPO IS NOT USEABLE YET!

# TEST

# StorageManager

## Intro - What is this all about

The new `Storage Access Framework (SAF)` introduced with android 5 does enforce you to be used on android 6. So when accessing **local** storages like internal storage and sd cards, you now have to differentiate between primary storage and secondary storage. You will be able to use the `File` class to access and modify files on the primary storage and you will have to get read/write permissions to access files on the secondary storage and use the `DocumentFile` class to access and modify files there.

Additionally, the `MediaStore` does still index primary and secondary storages and mix them. So you have to update it as well whenever you edit files on any storage.

**This library is meant for local files only!** The `SAF` does offer access to online data as well, but this library currently doesn't address this data.

## Overview - Main functions of this library

* handle permissions for secondary storages - this library offers you the functions to gain, persist and check permissions that may be necessary for reading and modifying files
* offers wrapper classes like `IFolder` and `IFile` that wrap a `File` or `DocumentFile` and offers all the necessary functions to read or manipulate those wrapped files
* manages `MediaStore` updates
* allows to **copy**, **delete** and **move**  files, either on primary or secondary storage
* allows to efficiently make **batch copies**, **batch deletes**, **batch moves** => `MediaStore` updates will be called in batch operations

## Wiki/FAQ

### 1. Setup

#### 1.1 Main setup

#### 1.2 Additional setup

### 2. Usage

**TODO**
