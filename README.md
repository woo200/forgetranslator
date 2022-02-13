# Auto Translate

This mod automatically translates incoming and outgoing chat messages in minecraft
To use this mod, you MUST get a DeepL api key. [How To Get One](#getting-a-deepl-key)
If you would like to modify this, feel free to.

### Getting a DeepL key
- Step 1) Go to https://deepl.com/
- Step 2) Click "Start free trial" at the top right corner
- Step 3) Click "For developers"
- Step 4) Click "Sign up for free"
- Step 5) Enter your email and create a password.
- Step 6) Enter your information and a card for verification
- Step 7) Go to https://www.deepl.com/pro-account/summary and scroll down
- Step 8) Copy the authentication key
- Step 9) Launch minecraft, and type `/setkey <API KEY HERE>`

### Configuration
The main configuration file is located at `<minecraft dir>/config/translator.config`
Currently, it only contains 1 config option:
- api_key - Your DeepL API key

### Developers
I was going to generate a JavaDoc, but my IDE doesn't want to work, but the comments are still there, so you can poke around and figure out how it works.