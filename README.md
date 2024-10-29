# Webpush Sample

[![webpush-sample][project-logo]][project-url]

Small demo application that uses [Push API](https://developer.mozilla.org/en-US/docs/Web/API/Push_API) and [Notifications API](https://developer.mozilla.org/en-US/docs/Web/API/Notifications_API) to show browser push notifications. 

[Report Bug](https://github.com/artemoons/webpush-sample/issues/new?assignees=artemoons&labels=bug&projects=&template=bug_report.md&title=%5BBUG%5D) - [Request Feature](https://github.com/artemoons/webpush-sample/issues/new?assignees=artemoons&labels=enhancement&projects=&template=feature_request.md&title=%5BREQUEST%5D)

## About

I tried to simplify this app as much as possible, so it's very easy to understand how it works. The only thing which is complex here - cryptographical service, implementation of RFC standard.

After launch push messages can be sent:
* via POST request to controller -- in this case "title" and "message" can be customized;

  ![sample-message-1](D:\projects\my\webpush-sample\webpush\readme_sources\sample-message.png)
* via web page -- in this case "title" is predefined, but "message" is customizeable too.

  ![sample-message-2](D:\projects\my\webpush-sample\webpush\readme_sources\sample-message-2.png)


## Getting Started
### Required dependencies

* Java 17
* Maven
* Spring Boot 3.3.5

### Setup

It's not necessary to setup any startup parameters, just pull repository and run.

### Executing program

Use IDE to run `WebpushApplication.java` or type in CMD `mvn spring-boot:run`. After application has started, go to
`http://localhost:8080` and hit "Subscribe" button. Then open Postman or cURL and send this request:
```bash
curl --location 'localhost:8080/api/v1/send' \
--header 'Content-Type: application/json' \
--data '{
    "title": "💭 Web push test",
    "body": "That'\''s how it works! Even with emoji 😎"
}'
```
If push notification is not being displayed, check that browser is inactive and tab is not open.

File with this request is located in `help_files/SendMessage.http`.

## Troubleshooting

Currently, I didn't find a reason why push is not being shown when tab is active in Chrome or Firefox, that's why it's not possible to
send message from root page (index.html). If you subscribe from these two browsers and try to send message
from one of it, you will see that message is sent and push is displayed from inactive browser.

Also I found some caching issues while working with *.js scripts and "Disable Cache" setting didn't help to solve it. So
keep this in mind! Currently, only Microsoft Edge (130.0.2849.52 (Official build)) worked well with loading actual *.js
scripts and displaying push from active tab.

## To-Do List / Backlog

- [ ] Troubleshoot why push is not appearing when tab is active (or may be it's connected with cached scripts)
- [ ] Refactor CryptoService
- [ ] Upgrade PushMessage DTO and add more fields, [supported by standard](https://developer.mozilla.org/en-US/docs/Web/API/Notifications_API) 

## Version History

* 0.0.1 (28 OCT 2024)
    * Initial Release

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Authors / Contributors

Artem Utkin  
[tema-utkin.ru](https://tema-utkin.ru)

## Acknowledgments

* [Sending Web Push Notifications with Java](https://golb.hplar.ch/2019/08/webpush-java.html)
* [Simple push demo](https://simple-push-demo.vercel.app/)
* [webpush-java](https://github.com/web-push-libs/webpush-java/wiki/Usage-Example)


[project-logo]: readme_sources/icons8-message-96.png
[project-url]: https://github.com/artemoons/webpush-sample