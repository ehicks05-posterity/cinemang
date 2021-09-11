# useful scripts

```
./gradlew.bat build

./gradlew.bat bootrun

./gradlew bootRun --args='--spring.profiles.active=dev'
```

# history

- 2015 - servlets+jsps, oMDB
- 2016 - first rumblings of EOI
- 2019 - ditch homegrown framework to spring, ditch jsp for thymeleaf, switch beans to - kotlin data classes to escape some java verbosity
- 2020 - ditch omdb for tmdb, add bulma
- 2021 - failed experiments with spring cloud, successful experiments with dokku
- 2021 - rewrite to supabase DB+BE API, react FE, and node dataloader scripts. contained in new repos: ehicks05/cinemang-backend ehicks05/cinemang-frontend (may not be public yet)
