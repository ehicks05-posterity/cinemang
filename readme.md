# status

2024-06-01: dokku deployment and dokku postgres instance deleted, cloudflare dns entry removed.

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

# screenshots

desktop:

![image](https://github.com/ehicks05-posterity/cinemang/assets/666393/b1ba87a9-3d5b-4e90-83a5-022e519ce251)

desktop search form:

![image](https://github.com/ehicks05-posterity/cinemang/assets/666393/8ac66bbc-3a9c-43a2-b713-93a83be95467)

search results as a table:

![image](https://github.com/ehicks05-posterity/cinemang/assets/666393/e9e301c1-72c0-4e1b-b0f0-d28508192965)

mobile (ok down to around 400px wide):

![image](https://github.com/ehicks05-posterity/cinemang/assets/666393/a3a9f1d9-e983-4213-9dab-c980c0f0be55)
