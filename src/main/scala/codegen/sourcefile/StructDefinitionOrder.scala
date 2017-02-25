package codegen.sourcefile

import codegen.types._

object StructDefinitionOrder {

  /**
    * Lists all the dependencies a struct has on other user-defined structs
    * @param struct Struct definition
    * @param dependencies List of the names of other structs the struct depends on. The name
    *                     of the structs should not include the * operator if the field is a
    *                     pointer field
    */
  private case class StructDependencies(struct: StructDefinition, dependencies: Seq[String])

  /**
    * Computes the correct order in which structs need to be defined. If one struct contains
    * another struct as one of its field, then the referenced struct must be declared before
    * the containing struct. If the struct definitions contain circular dependencies, then
    * None is returned.
    * @param structs List of struct definitions
    * @return List of struct definitions in the order that they must be listed to satisfy
    *         all dependencies
    */
  def apply(structs: Seq[StructDefinition]): Option[Seq[StructDefinition]] = {
    val structNames = structs.map(_.name)
    val structDependencies = structs.map(struct => dependenciesForStruct(struct, structNames))

    definitionOrder(structDependencies)
   }

  /**
    * Gets the set of dependencies the given struct has on other user-defined structs.
    * @param struct Struct for which to get the dependencies
    * @param allStructNames List of names of all structs to be defined
    * @return The struct's dependencies
    */
  private def dependenciesForStruct(struct: StructDefinition, allStructNames: Seq[String]): StructDependencies = {
    val dependencies = struct.fields.flatMap({
      case SimpleStructField(_, typeDeclaration) if isStructField(typeDeclaration, allStructNames) => Some(removePointer(typeDeclaration))
      case FixedArrayStructField(_, elementType, _) if isStructField(elementType, allStructNames) => Some(elementType)
      case _ => None
    })

    StructDependencies(struct, dependencies)
  }

  /**
    * Determines if the provided field references another struct and therefore indicates a
    * dependency on that other struct's definition based on the fields type declaration
    * @param typeDeclaration Type declaration string of the field
    * @param allStructNames List of the names of all structs that are defined together
    * @return True if the field reference's another struct
    */
  private def isStructField(typeDeclaration: String, allStructNames: Seq[String]): Boolean = {
    val pointerLessType = removePointer(typeDeclaration)
    allStructNames.contains(pointerLessType)
  }

  /**
    * Removes the pointer modifier (*) from the provided type declaration string if it
    * is a pointer type
    * @param typeDeclaration Type declaration string
    * @return Type declaration string without a '*'
    */
  private def removePointer(typeDeclaration: String): String = {
    typeDeclaration.replace("*", "").trim
  }

  /**
    * Computes the order in which structs should be defined so that all dependency relations between
    * structs are satisfied. Returns None if there are any circular dependencies
    * @param dependencies - List of all struct dependencies
    * @return None if there are circular dependencies, the list of struct definitions in the order that
    *         they should be declared otherwise
    */
  private def definitionOrder(dependencies: Seq[StructDependencies]): Option[Seq[StructDefinition]] = {

    def definitionOrderRecursive(dependencies: Seq[StructDependencies], accumulatedOrdering: Seq[StructDefinition]): Option[Seq[StructDefinition]] = {
      if(dependencies.isEmpty) {
        Some(accumulatedOrdering)
      } else {
        // Get the list of struct definitions that are not dependent on the
        // definitions of any other structs
        val (nonDependentStructs, dependentStructs) = dependencies.partition(_.dependencies.isEmpty)

        if(nonDependentStructs.isEmpty) {
          // We were unable to find any structs that were not dependent on the
          // definition of other structs which means there is a circular dependency
          None
        } else {
          // The structs that do not have any dependencies can now be added to the
          // end of the definition order list.
          val newOrdering = accumulatedOrdering ++ nonDependentStructs.map(_.struct)

          // Remove the structs that we added to the definition ordering as dependencies
          // from the remaining structs since the dependencies on those structs have now
          // been satisfied
          val nonDependentStructNames = nonDependentStructs.map(_.struct.name)
          val newDependencies = dependentStructs.map(oldDependencies => removeDependencies(oldDependencies, nonDependentStructNames))

          definitionOrderRecursive(newDependencies, newOrdering)
        }
      }
    }

    definitionOrderRecursive(dependencies, Nil)
  }

  /**
    * Remove the given set of structs from the given set of struct dependencies
    * @param structDependencies Set of dependencies from which to remove the set of structs
    * @param toRemoveNames List of names of structs to remove from the dependency set
    * @return A new dependency set with the specified struct names removed
    */
  private def removeDependencies(structDependencies: StructDependencies, toRemoveNames: Seq[String]): StructDependencies = {
    val newDependencies = structDependencies.dependencies.filterNot(dependency => toRemoveNames.contains(dependency))

    StructDependencies(structDependencies.struct, newDependencies)
  }

}
