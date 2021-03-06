package nextstep.ladder;

import nextstep.ladder.domain.IndexedName;
import nextstep.ladder.domain.Ladder;
import nextstep.ladder.domain.Line;
import nextstep.ladder.domain.Spoke;
import nextstep.ladder.utils.ImmutableMaps;
import nextstep.ladder.view.InputView;
import nextstep.ladder.view.LadderView;
import nextstep.ladder.view.ResultView;

import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Controller {

    private static final Random RANDOM = new Random();
    private static final String ALL = "all";
    private final InputView inputView = new InputView();
    private final ResultView resultView = new ResultView(new OutputStreamWriter(System.out));

    public void play() {
        List<IndexedName> players = IndexedName.wrap(inputView.requestPlayers());
        List<IndexedName> goals = IndexedName.wrap(inputView.requestGoal());
        int ladderHeight = inputView.requestHeight();

        Ladder ladder = Ladder.of(createSpokes(ladderHeight, players.size()), goals);

        resultView.printLadder(new LadderViewAdapter(goals, ladder), IndexedName.unwrap(players));

        Map<String, String> moveResult = ladder.moveForAll(players);

        String name;
        do {
            name = inputView.requestPlayerName();
            resultView.printResult(getMoveResult(moveResult, name));
        } while (!name.equals(ALL));
    }

    private Stream<Spoke> createSpokes(int ladderHeight, int width) {
        return IntStream.range(0, ladderHeight)
                .mapToObj(__ -> Spoke.fromCount(width - 1, RANDOM::nextBoolean));
    }

    private Map<String, String> getMoveResult(Map<String, String> result, String name) {
        if (name.equals(ALL)) {
            return result;
        }
        return ImmutableMaps.of(name, result.get(name));
    }

    private static class LadderViewAdapter implements LadderView {

        private final List<IndexedName> goals;
        private final Ladder ladder;

        public LadderViewAdapter(List<IndexedName> goals, Ladder ladder) {
            this.goals = goals;
            this.ladder = ladder;
        }

        @Override
        public List<String> getGoals() {
            return IndexedName.unwrap(goals);
        }

        @Override
        public void forEachLine(Consumer<List<Boolean>> singleLineConsumer) {
            ladder.forEach(Line::toSpokeStream);

        }
    }
}
