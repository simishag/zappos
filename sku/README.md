Zappos Code Challenge
Judd Bourgeois
2014-02-11

This is a Java application that loads a list of SKU numbers from a text file.
The Product API is used to retrieve information for each SKU, specifically
the field "defaultImageUrl". Each image is retrieved and saved locally.

I developed this app using Java 1.6, Spring Tool Suite (Eclipse) and 
Maven 3. The following additional Java libraries were used:

json-simple: JSON parser & toolkit
commons-lang3: StringUtils
commons-httpclient: HTTP communication
commons-io: file I/O
log4j: logging

junit is included for unit testing but I did not implement any
tests due to time constraints.

The app can be run in Maven with:

maven exec:java -Dexec.mainClass=zappos.sku.ImageApp -Dexec.args="skus.txt"

The instructions provided indicate that 4 images should be returned.
However, I was only able to retrieve 3 images. I checked the JSON
response and found that on SKU 7860495, defaultImageUrl is set to null.

There are some minor improvements that could be made given more time.
The SKU input file could be streamed, rather than read in all at once.
Multiple threads could be used to retrieve and save the images in parallel
rather than serially. Both of these steps could be put together with
the use of thread pools.

Test 1

Test 2