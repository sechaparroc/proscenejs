package ik.collada.colladaParser.colladaLoader;

import frames.processing.Scene;
import ik.collada.animation.AnimatedModel;
import ik.collada.animation.Animation;
import ik.collada.animation.Mesh;
import ik.collada.animation.SkinningData;
import ik.collada.colladaParser.xmlParser.XmlNode;
import ik.collada.colladaParser.xmlParser.XmlParser;
import processing.core.PGraphics;
import processing.core.PShape;

import java.io.File;

public class ColladaLoader {
    //TODO : Update
    public static AnimatedModel loadColladaModel(String colladaFile, String dae, String tex, Scene scene, int maxWeights) {
        XmlNode node = XmlParser.loadXmlFile(colladaFile + dae);

        AnimatedModel model = new AnimatedModel(scene);

        SkinLoader skinLoader = new SkinLoader(node.getChild("library_controllers"), maxWeights);
        SkinningData skinningData = skinLoader.extractSkinData();

        SkeletonLoader jointsLoader = new SkeletonLoader(node.getChild("library_visual_scenes"), skinningData.jointOrder);
        jointsLoader.extractBoneData(model);

        GeometryLoader g = new GeometryLoader(node.getChild("library_geometries"), skinningData.verticesSkinData);
        Mesh meshData = g.extractModelData();
        model.setModel(meshData.generatePShape(scene.frontBuffer(), colladaFile + tex));
        return model;
    }

    public static Animation loadColladaAnimation(String colladaFile) {
        XmlNode node = XmlParser.loadXmlFile(colladaFile);
        XmlNode animNode = node.getChild("library_animations");
        XmlNode jointsNode = node.getChild("library_visual_scenes");
        AnimationLoader loader = new AnimationLoader(animNode, jointsNode);
        Animation animData = loader.extractAnimation();
        return animData;
    }

}