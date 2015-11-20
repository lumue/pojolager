# pojolager

simplistic embedded pojo persistence

 * pluggable serialization (use json as default)
 * each object is stored as a single file
 * use object key as filename
 * one PojoLager per Java type
 * PojoLager extends Map Interface
 * in memory indices can be defined on pojo properties