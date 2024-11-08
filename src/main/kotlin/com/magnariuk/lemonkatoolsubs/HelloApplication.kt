package com.magnariuk.lemonkatoolsubs

import com.magnariuk.lemonkatoolsubs.data.CacheController
import com.magnariuk.lemonkatoolsubs.data.classes.Ass
import com.magnariuk.lemonkatoolsubs.data.classes.Actor
import com.magnariuk.lemonkatoolsubs.data.Parser
import javafx.application.Application
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.Stage
import org.controlsfx.control.CheckComboBox
import java.awt.Desktop
import java.io.File

class HelloApplication : Application() {
    val cacheController: CacheController = CacheController()
    val assParser: Parser = Parser()


    override fun start(stage: Stage) {
        var ass: Ass? = null
        var charactersList: List<String> = listOf()
        var actorsList: List<String> = cacheController.getCache()?.actors?.map { it.actorName }!!

        val assLocation = TextField().apply {
            prefWidth = 300.0
        }
        val actorsListItem = Text()
        val charactersListView = CheckComboBox<String>(FXCollections.observableArrayList(charactersList))

        val actorComboBox = ComboBox<String>().apply {
            actorsList.let { items.addAll(it) }
            selectionModel.selectFirst()
            val actor = cacheController.getCache()?.actors?.find { it.actorName == selectionModel.selectedItem }
            actor?.characterNames?.forEach { characterName ->
                val index = charactersList.indexOf(characterName)
                if (index != -1) {
                    charactersListView.checkModel.check(index)
                }
            }
            setOnAction {
                val selectedActorName = selectionModel.selectedItem
                val actor = cacheController.getCache()?.actors?.find { it.actorName == selectedActorName }

                charactersListView.checkModel.clearChecks()

                actor?.characterNames?.forEach { characterName ->
                    val index = charactersList.indexOf(characterName)
                    if (index != -1) {
                        charactersListView.checkModel.check(index)
                    }
                }
            }
        }


        val updateActorsList = {
            actorsListItem.text = actorsList.joinToString(", ") ?: ""

        }





        val fileChooserBox = HBox(
            assLocation,
            Button("Відкрити").apply {
                setOnAction {
                    val fileChooser = FileChooser().apply {
                        extensionFilters.add(FileChooser.ExtensionFilter("Дупця", "*.ass"))
                        title = "Оберіть субтитри"
                        initialDirectory = File(assLocation.text).parentFile.takeIf { it?.exists() == true } ?: File(System.getProperty("user.home"))
                    }
                    val selectedFile = fileChooser.showOpenDialog(null)
                    selectedFile?.let {
                        assLocation.text = it.absolutePath
                        ass = assParser.parseAssFile(it.absolutePath)
                        charactersList = ass?.getAllActors()?.distinct() ?: listOf()
                        actorsList = cacheController.getCache()?.actors?.map { it.actorName }!!
                        updateActorsList()
                        charactersListView.items.addAll(0, charactersList)
                    }
                }
            }
        ).apply { alignment = Pos.CENTER }

        val addActorField = TextField()
        val addActorButton = Button("Додати").apply {
            setOnAction {
                if (addActorField.text.isNotBlank() && !actorsList.contains(addActorField.text)) {
                    val cache = cacheController.getCache()
                    cache?.actors?.add(Actor(addActorField.text, mutableListOf()))
                    cache?.let { cacheController.saveCache(it) }
                    actorsList = cacheController.getCache()?.actors?.map { it.actorName }!!
                    updateActorsList()
                    actorComboBox.items.add(addActorField.text)
                }
            }
        }
        val removeActorButton = Button("Видалити").apply {
            setOnAction {
                if (addActorField.text.isNotBlank()) {
                    val cache = cacheController.getCache()
                    cache?.actors?.removeIf { it.actorName == addActorField.text }
                    cache?.let { cacheController.saveCache(it) }
                    actorsList = cacheController.getCache()?.actors?.map { it.actorName }!!
                    updateActorsList()
                    actorComboBox.items.remove(addActorField.text)

                }
            }
        }


        val assignCharactersButton = Button("Призначити персонажів").apply {
            setOnAction {
                val selectedActor = actorComboBox.value
                val selectedCharacters = charactersListView.checkModel.checkedItems
                val cache = cacheController.getCache()

                cache?.actors?.find { it.actorName == selectedActor }?.characterNames?.addAll(selectedCharacters)
                cache?.let { cacheController.saveCache(it) }
            }
        }

        val leftSideBox = VBox(actorsListItem, HBox(addActorField, addActorButton, removeActorButton)).apply {
            alignment = Pos.TOP_CENTER
        }

        val rightSideBox = VBox(actorComboBox, charactersListView, assignCharactersButton).apply {
            alignment = Pos.TOP_CENTER
        }

        val mainVBox = VBox(
            fileChooserBox,
            HBox(leftSideBox, rightSideBox).apply {
                alignment = Pos.CENTER
            },
            Button("Створити (переконайтеся, що відкрили субтитри '.ass')").apply {
                setOnAction {
                    val dirChooser = DirectoryChooser().apply {
                        title = "Оберіть субтитри"
                        initialDirectory = File(assLocation.text).parentFile.takeIf { it?.exists() == true } ?: File(System.getProperty("user.home"))
                    }
                    val selectedDir = dirChooser.showDialog(null)
                    selectedDir?.let {
                        val actors = cacheController.getCache()?.actors
                        actors?.forEach { actor ->
                            assParser.createSubRip(it.absolutePath+"/${actor.actorName}.srt", ass!!, actor.characterNames.toList())
                            Desktop.getDesktop().open(it)

                        }
                    }
                }
            }
        ).apply { alignment = Pos.TOP_CENTER }

        val scene = Scene(mainVBox, 800.0, 600.0)
        stage.scene = scene
        stage.title = "Modpack Updater"
        stage.show()
    }
}


fun main() {
    Application.launch(HelloApplication::class.java)
}