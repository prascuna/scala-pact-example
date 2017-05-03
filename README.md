# scala-pact-example

A sample project to play around [Pact](https://docs.pact.io/) and [scala-pact](https://github.com/ITV/scala-pact) 


The sample consists in three applications:  


```
   
┏━━━━━━┓                           ┏━━━━━━┓                           ┏━━━━━━┓ 
┃      ┃  ---------------------->  ┃      ┃  ---------------------->  ┃      ┃
┃ App1 ┃   POST /data/forward      ┃ App2 ┃   POST /data/             ┃ App3 ┃
┃      ┃  <----------------------  ┃      ┃  <----------------------  ┃      ┃
┗━━━━━━┛ Location: http://app3/123 ┗━━━━━━┛ Location: http://app3/123 ┗━━━━━━┛ 
```


App1 performs a POST the App2, that forwards this request to App3 that eventually stores some data in its datastore.  
The return value will be the Location header of the machine in which the data got stored.


__App1__ is a __Consumer__ of App2  
__App2__ is a __Provider__ for App1 and a __Consumer__ of App3  
__App3__ is a __Producer__ for App2  

## Build
### App1
Being just a __Consumer__, we need to generate the Pact file and publishing it to the Pact broker

`sbt pact-publish`  

N.B.: This command will also run `pact-test` used to generate the Pact files

### App2
Being a __Consumer__ we need to generate and publish the Pact file, but let's not run  `sbt pact-publish` yet  

Being also a __Producer__ we need to need to verify the existing contracts with the other consumers.
In order do that, we need to run our tests with `sbt test` but let's not run it yet :)  
We cannot run the test just yet because we first need to stub our dependencies, that's why we're going to run this command first:  

`export APP_PORT=19092;export SERVICE_FORWARD_URI="http://localhost:1234/data";sbt pact-stubber`

This command will create a *Stubber* on port 1234 that will behave according to the Pacts retrieved from the broker.  
Unfortunately this command also runs the tests, so we will see some errors, and that's also why we're passing those environment variables.
Those variables are not needed by te stubber itself but by the fact that the test will run the application.  
Now that the stubber is running, we can finally we can finally run our tests, pointing our application to the stubber URI.

`export APP_PORT=9092;export SERVICE_FORWARD_URI="http://localhost:1234/data";sbt pact-publish`

This will also run `sbt test` and verify then our provider.

### App3
__App3__ is just a __Producer__ so the same steps as App2 apply:

`export APP_PORT=19092;export SERVICE_FORWARD_URI="http://localhost:1234/data";sbt pact-stubber`  
`export APP_PORT=9092;export SERVICE_FORWARD_URI="http://localhost:1234/data";sbt pact-publish`
