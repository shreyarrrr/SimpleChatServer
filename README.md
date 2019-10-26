# SimpleChatServer
Author: Shreya Roy

Version: 11/27/18

As a frequent user of social media, like many others from my generation, I became curious about how a chat server worked and developed a program that demonstrated a simple implementation of one.

## Usage

In order to use the simple chat server, the ChatServer class is ran first. The message "hh:mm:ss Server waiting for Clients on port 1500." pops up, which means that the server is ready for clients to join. After that, the ChatClient class is run once if there is one person in the chat server, twice if there are two, etc. Now, the user can type in message in the console for one of the chat clients, and that message will show up on the server and in the console for all other clients. If one of the clients says a "bad word"--a word or phrase that is listed on badwords.txt--the ChatFilter class will censor the word/phrase. When the conversation is over, closing the chat server will also stop all of the clients from running.
