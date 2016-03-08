#### ONLY PREPARING FOR FIRST RELEASE - REPO IS NOT USEABLE YET AND SOURCE IS FAR BEHIND CURRENT LIBRARY STATE AND NOT WORKING!

# StorageManager
> Manage `File` and `DocumentFile` in a common wrapper class

**This library is meant for local files only!** 

## Intro - What is this all about

The new `Storage Access Framework (SAF)` introduced with android 5 does enforce you to be used on android 6. So when accessing *local storages* like internal storage and sd cards, you now have to differentiate between primary storage and secondary storage. You will be able to use the `File` class to access and modify files on the primary storage and you will have to get read/write permissions to access files on the secondary storage and use the `DocumentFile` class to access and modify files there.

Additionally, the `MediaStore` does still index primary and secondary storages and mix them. So you have to update it as well whenever you edit files on any storage.

This library addresses local files only, although `SAF` does offer access to online data as well. But this is out of the scope of this library, altough it should be extendable to support them as well. USB sticks would fit the scope of this library but are not addressed yet.

## Overview - Main functions of this library

* Permissions
 * handle permissions for secondary storages - this library offers you the functions to gain, persist and check permissions that may be necessary for reading and modifying files
* Files
 * offers wrapper class `IFile` that wraps a `File` (wrapped in `StorageFile`) or `DocumentFile` (wrapped in `StorageDocument`) and offers all the necessary functions to read or manipulate those wrapped files
 * allows to create `IFile` from any path, the library will take care to create the correct `IFile` (depending on the location of the path you'll get a `StorageFile` or a `StorageDocument`)
 * allows to **copy**, **delete** and **move**  files, either on primary or secondary storage
 * allows to efficiently make **batch copies**, **batch deletes**, **batch moves** => `MediaStore` updates will be called in batch operations
* MediaStore
 * manages `MediaStore` automatically
 * offers `MediaStoreFileData` and `MediaStoreFolderData` class that holds the data from the `MediaStore` and that can be loaded with the folders/files (if you need this data)
* Folder
 * **list folders** on primary and secondary storage efficiently => load folders with/-out content and with/-out `MediaStore` data 
 * **load files in folder** => load them with-/out `MediaStore`data
* Primary/Secondary storage (internal, sd card) 
 * offers functions to get internal storage path and sd card path

**TODO**

## Wiki/FAQ

### 1. Setup

#### 1.1 Main setup

#### 1.2 Additional setup

### 2. Usage
