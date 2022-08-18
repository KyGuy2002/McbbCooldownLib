# McbbCooldownLib
**NOTE:** This repo has been archived as the code included was moved to the api wrapper plugin.


## Maven Dependancy
```
<dependency>
  <groupId>net.mcblockbuilds</groupId>
  <artifactId>McbbCooldownLib</artifactId>
  <version>LATEST</version>
  <scope>compile</scope>
</dependency>
```

## Constructer
```
private static final Cache<type> cache = new Cache<>(plugin, (uuid, data)-> {
  // Here you can access the objects uuid and data.
}, cooldown);
```

`type`: The object that will be stored.  
`plugin`: The plugins instance.  
`cooldown`: Seconds between saves.  
