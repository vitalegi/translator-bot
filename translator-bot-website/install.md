# Install

This Bot, once configured, will use Amazon AWS translation service to automatically translate messages in specific channels.

Since the service it's using is pay-per-use (15$ every 1 million characters), some limits are configured:

- Monthly number of characters that can be translated in a server
- Monthly number of characters that can be translated in a server for each user
- Of each message, only the first 1000 characters are translated, everything else is ignored


Supported languages: <https://docs.aws.amazon.com/translate/latest/dg/what-is-languages.html>

Pricing: <https://aws.amazon.com/it/translate/pricing/>

## Permission required

Only read/write messages.

## Info required to complete the configuration

List of channels to be connected together. Multiple groups of channels can be created.

It's not important to group the channels in the same category.

Avoid using emoji in channel's name.
