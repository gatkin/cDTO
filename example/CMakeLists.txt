cmake_minimum_required (VERSION 2.8)

project (ExampleProtocol)

include_directories(cJSON)

set(CJSON_SOURCE_FILES cJSON/cJSON.c cJSON/cJSON_Utils.c)
set(SOURCE_FILES main.c github_issues.cdto.c github_issues.cdto.json.c ${CJSON_SOURCE_FILES})

add_executable(main ${SOURCE_FILES})
target_link_libraries(main m)